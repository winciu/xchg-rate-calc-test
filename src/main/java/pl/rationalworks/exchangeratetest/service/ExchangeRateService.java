package pl.rationalworks.exchangeratetest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.rationalworks.exchangeratetest.integration.fixer.model.LatestRates;
import pl.rationalworks.exchangeratetest.model.ExchangeRate;
import pl.rationalworks.exchangeratetest.model.ExchangeRateId;
import pl.rationalworks.exchangeratetest.properties.SpreadProperties;
import pl.rationalworks.exchangeratetest.repository.ExchangeRatesRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRatesRepository ratesRepository;
    private final ExchangeRateCalculator rateCalculator;
    private final SpreadProperties spreadProperties;

    public void saveFixerRates(LatestRates rates) {
        List<ExchangeRate> exchangeRates = rates.getRates().entrySet().stream().map(entry -> {
            ExchangeRateId rateId = new ExchangeRateId(rates.getDate(), entry.getKey());
            return new ExchangeRate(rateId, rates.getTimestamp(), entry.getValue());
        }).toList();
        ratesRepository.saveAll(exchangeRates);
    }

    public Optional<BigDecimal> calculateExchangeRate(Currency fromCurrency, Currency toCurrency, LocalDate date) {
        Optional<ExchangeRate> rateFrom;
        Optional<ExchangeRate> rateTo;
        if (isNull(date)) {
            rateFrom = ratesRepository.findFirstByExchangeRateId_TargetCurrencyOrderByExchangeRateId_DateDesc(fromCurrency);
            rateTo = ratesRepository.findFirstByExchangeRateId_TargetCurrencyOrderByExchangeRateId_DateDesc(toCurrency);
        } else {
            rateFrom = ratesRepository.findExchangeRateByExchangeRateId_DateAndExchangeRateId_TargetCurrency(date, fromCurrency);
            rateTo = ratesRepository.findExchangeRateByExchangeRateId_DateAndExchangeRateId_TargetCurrency(date, toCurrency);
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
}
