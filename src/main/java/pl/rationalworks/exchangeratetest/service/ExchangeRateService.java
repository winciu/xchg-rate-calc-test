package pl.rationalworks.exchangeratetest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pl.rationalworks.exchangeratetest.integration.fixer.FixerExchangeRatesProvider;
import pl.rationalworks.exchangeratetest.integration.fixer.FixerRatesProviderException;
import pl.rationalworks.exchangeratetest.integration.fixer.model.LatestRates;
import pl.rationalworks.exchangeratetest.model.ExchangeRate;
import pl.rationalworks.exchangeratetest.model.ExchangeRateId;
import pl.rationalworks.exchangeratetest.model.dto.ExchangeRates;
import pl.rationalworks.exchangeratetest.properties.ServiceProperties;
import pl.rationalworks.exchangeratetest.properties.SpreadProperties;
import pl.rationalworks.exchangeratetest.repository.ExchangeRatesRepository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import pl.rationalworks.exchangeratetest.model.Currency;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final FixerExchangeRatesProvider fixerExchangeRatesProvider;
    private final ExchangeRatesRepository ratesRepository;
    private final ExchangeRateCalculator rateCalculator;
    private final SpreadProperties spreadProperties;
    private final ServiceProperties serviceProperties;

    public Optional<ExchangeRates> fetchRatesFromFixer() {
        log.info("Start fetching Fixer exchange rates ....");
        LatestRates fixerRates;
        try {
            fixerRates = fixerExchangeRatesProvider.provideExchangeRates();
        } catch (FixerRatesProviderException e) {
            log.error("Cannot fetch rates.", e);
            return Optional.empty();
        }
        log.info("Rates fetched. Timestamp: {}", fixerRates.getTimestamp().toString());
        return Optional.of(new ExchangeRates(
                fixerRates.getTimestamp(), fixerRates.getBase(), fixerRates.getDate(), fixerRates.getRates()));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void saveOrUpdateRates(ExchangeRates exchangeRates) {
        Optional<List<ExchangeRate>> latestRates = ratesRepository.findAllByExchangeRateId_Date(exchangeRates.date());
        if (latestRates.isPresent()) {
            // utilize batch updates (see config for required settings to make it work)
            latestRates.get().forEach(rate -> {
                rate.setRate(exchangeRates.rates().get(rate.getExchangeRateId().getTargetCurrency()));
                rate.setTimestamp(exchangeRates.timestamp());
            });
            ratesRepository.saveAll(latestRates.get());
        } else {
            ratesRepository.saveAll(mapToEntities(exchangeRates));
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Optional<BigDecimal> calculateExchangeRate(Currency fromCurrency, Currency toCurrency, LocalDate date) {
        Optional<ExchangeRate> rateFrom;
        Optional<ExchangeRate> rateTo;
        if (isNull(date)) {
            rateFrom = readRate(fromCurrency);
            rateTo = readRate(toCurrency);
        } else {
            rateFrom = readRate(fromCurrency, date);
            rateTo = readRate(toCurrency, date);
        }
        if (rateFrom.isEmpty() || rateTo.isEmpty()) {
            return Optional.empty();
        }
        BigDecimal fromCurrencyRate = rateFrom.get().getRate();
        BigDecimal toCurrencyRate = rateFrom.get().getRate();
        BigDecimal fromCurrencySpread = spreadProperties.obtainCurrencySpread(fromCurrency);
        BigDecimal toCurrencySpread = spreadProperties.obtainCurrencySpread(toCurrency);
        BigDecimal calculatedExchangeRate = rateCalculator.calculateExchangeRate(fromCurrencyRate, toCurrencyRate,
                fromCurrencySpread, toCurrencySpread);
        return Optional.of(calculatedExchangeRate);
    }

    Optional<ExchangeRate> readRate(Currency fromCurrency, LocalDate date) {
        Optional<ExchangeRate> rate = ratesRepository.findExchangeRateByExchangeRateId_DateAndExchangeRateId_TargetCurrency(date, fromCurrency);
        rate.ifPresent(exchangeRate -> ratesRepository.updateRequestCounter(exchangeRate.getExchangeRateId()));
        return rate;
    }

    Optional<ExchangeRate> readRate(Currency fromCurrency) {
        Optional<ExchangeRate> rate = ratesRepository.findFirstByExchangeRateId_TargetCurrencyOrderByExchangeRateId_DateDesc(fromCurrency);
        rate.ifPresent(exchangeRate -> ratesRepository.updateRequestCounter(exchangeRate.getExchangeRateId()));
        return rate;
    }

    public Optional<ExchangeRates> obtainRatesByDate(LocalDate date) {
        Optional<List<ExchangeRate>> latestRates = ratesRepository.findAllByExchangeRateId_Date(date);
        if (latestRates.isPresent()) {
            return mapToExchangeRates(latestRates.get());
        } else {
            return Optional.empty();
        }
    }

    private Optional<ExchangeRates> mapToExchangeRates(List<ExchangeRate> exchangeRateList) {
        Map<Currency, BigDecimal> ratesMap = exchangeRateList.stream()
                .collect(Collectors.
                        toMap(rate -> rate.getExchangeRateId().getTargetCurrency(), ExchangeRate::getRate));
        if (!exchangeRateList.isEmpty()) {
            ExchangeRate exchangeRate = exchangeRateList.get(0);
            ExchangeRates exchangeRates = new ExchangeRates(
                    exchangeRate.getTimestamp(),
                    serviceProperties.getBaseCurrency(),
                    exchangeRate.getExchangeRateId().getDate(),
                    ratesMap);
            return Optional.of(exchangeRates);
        }
        return Optional.empty();
    }

    private List<ExchangeRate> mapToEntities(ExchangeRates exchangeRates) {
        return exchangeRates.rates().entrySet().stream().map(entry -> {
            ExchangeRateId rateId = new ExchangeRateId(exchangeRates.date(), entry.getKey());
            return new ExchangeRate(rateId, exchangeRates.timestamp(), entry.getValue(), BigInteger.ZERO);
        }).toList();
    }
}
