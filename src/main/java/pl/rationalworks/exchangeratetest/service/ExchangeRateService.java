package pl.rationalworks.exchangeratetest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pl.rationalworks.exchangeratetest.integration.fixer.FixerExchangeRatesProvider;
import pl.rationalworks.exchangeratetest.integration.fixer.FixerRatesProviderException;
import pl.rationalworks.exchangeratetest.integration.fixer.model.LatestRates;
import pl.rationalworks.exchangeratetest.model.Currency;
import pl.rationalworks.exchangeratetest.model.ExchangeRate;
import pl.rationalworks.exchangeratetest.model.dto.ExchangeRates;
import pl.rationalworks.exchangeratetest.model.mapper.ExchangeRateToDtoMapper;
import pl.rationalworks.exchangeratetest.properties.SpreadProperties;
import pl.rationalworks.exchangeratetest.repository.ExchangeRatesRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final FixerExchangeRatesProvider fixerExchangeRatesProvider;
    private final ExchangeRatesRepository ratesRepository;
    private final ExchangeRateCalculator rateCalculator;
    private final SpreadProperties spreadProperties;
    private final ExchangeRateToDtoMapper rateToDtoMapper;

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
        List<ExchangeRate> latestRates = ratesRepository.findAllByExchangeRateId_Date(exchangeRates.date());
        if (latestRates.isEmpty()) {
            ratesRepository.saveAll(rateToDtoMapper.mapToEntities(exchangeRates));
        } else {
            // utilize batch updates (see config for required settings to make it work)
            latestRates.forEach(rate -> {
                rate.setRate(exchangeRates.rates().get(rate.getExchangeRateId().getTargetCurrency()));
                rate.setTimestamp(exchangeRates.timestamp());
            });
            ratesRepository.saveAll(latestRates);
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Optional<BigDecimal> calculateExchangeRate(Currency fromCurrency, Currency toCurrency, LocalDate date) {
        Optional<ExchangeRate> rateFrom;
        Optional<ExchangeRate> rateTo;
        // In this if statements we are reading rates from a database by querying the database twice (to get 'from' and 'to' rates).
        // In case this way is not optimal, we could fetch those rates once using a modified query which returns a map.
        // See repository class javadoc for more details
        if (isNull(date)) {
            rateFrom = readRate(fromCurrency);
            rateTo = readRate(toCurrency);
        } else {
            rateFrom = readRate(fromCurrency, date);
            rateTo = readRate(toCurrency, date);
        }

        //those updates could be modified as well (by using were _id in (...)) if we return maps in the above queries (another approach)
        rateFrom.ifPresent(exchangeRate -> ratesRepository.updateRequestCounter(exchangeRate.getExchangeRateId()));
        rateTo.ifPresent(exchangeRate -> ratesRepository.updateRequestCounter(exchangeRate.getExchangeRateId()));

        if (rateFrom.isEmpty() || rateTo.isEmpty()) {
            return Optional.empty();
        }

        BigDecimal fromCurrencyRate = rateFrom.get().getRate();
        BigDecimal toCurrencyRate = rateTo.get().getRate();
        BigDecimal fromCurrencySpread = spreadProperties.obtainCurrencySpread(fromCurrency);
        BigDecimal toCurrencySpread = spreadProperties.obtainCurrencySpread(toCurrency);
        BigDecimal calculatedExchangeRate = rateCalculator.calculateExchangeRate(fromCurrencyRate, toCurrencyRate,
                fromCurrencySpread, toCurrencySpread);
        return Optional.of(calculatedExchangeRate);
    }

    Optional<ExchangeRate> readRate(Currency fromCurrency, LocalDate date) {
        return ratesRepository.findExchangeRateByExchangeRateId_DateAndExchangeRateId_TargetCurrency(date, fromCurrency);
    }

    Optional<ExchangeRate> readRate(Currency fromCurrency) {
        return ratesRepository.findFirstByExchangeRateId_TargetCurrencyOrderByExchangeRateId_DateDesc(fromCurrency);
    }

    public Optional<ExchangeRates> obtainRatesByDate(LocalDate date) {
        List<ExchangeRate> latestRates = ratesRepository.findAllByExchangeRateId_Date(date);
        if (latestRates.isEmpty()) {
            return Optional.empty();
        } else {
            return rateToDtoMapper.mapToExchangeRates(latestRates);
        }
    }

}
