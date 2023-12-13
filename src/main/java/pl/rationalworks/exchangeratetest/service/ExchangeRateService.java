package pl.rationalworks.exchangeratetest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.rationalworks.exchangeratetest.integration.fixer.model.LatestRates;
import pl.rationalworks.exchangeratetest.model.ExchangeOperationRepresentation;
import pl.rationalworks.exchangeratetest.model.ExchangeRate;
import pl.rationalworks.exchangeratetest.model.ExchangeRateId;
import pl.rationalworks.exchangeratetest.properties.SpreadProperties;
import pl.rationalworks.exchangeratetest.repository.ExchangeRatesRepository;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

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

    public ExchangeOperationRepresentation calculateExchangeRate(Currency from, Currency to, LocalDate date) {
        return null;
    }
}
