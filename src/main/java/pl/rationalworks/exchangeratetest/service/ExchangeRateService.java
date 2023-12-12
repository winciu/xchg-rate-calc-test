package pl.rationalworks.exchangeratetest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.rationalworks.exchangeratetest.integration.fixer.model.LatestRates;
import pl.rationalworks.exchangeratetest.model.ExchangeRate;
import pl.rationalworks.exchangeratetest.model.ExchangeRateId;
import pl.rationalworks.exchangeratetest.repository.ExchangeRatesRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRatesRepository ratesRepository;

    public void saveFixerRates(LatestRates rates) {
        List<ExchangeRate> exchangeRates = rates.getRates().entrySet().stream().map(entry -> {
            ExchangeRateId rateId = new ExchangeRateId(rates.getTimestamp(), ExchangeRateId.PROVIDER_NAME, rates.getBase(), entry.getKey());
            return new ExchangeRate(rateId, rates.getDate(), entry.getValue());
        }).toList();
        ratesRepository.saveAll(exchangeRates);
    }
}
