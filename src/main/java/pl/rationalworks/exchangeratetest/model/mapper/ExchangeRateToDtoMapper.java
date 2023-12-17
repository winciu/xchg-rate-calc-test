package pl.rationalworks.exchangeratetest.model.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.rationalworks.exchangeratetest.model.Currency;
import pl.rationalworks.exchangeratetest.model.ExchangeRate;
import pl.rationalworks.exchangeratetest.model.ExchangeRateId;
import pl.rationalworks.exchangeratetest.model.dto.ExchangeRates;
import pl.rationalworks.exchangeratetest.properties.ServiceProperties;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ExchangeRateToDtoMapper {

    private final ServiceProperties serviceProperties;

    public Optional<ExchangeRates> mapToExchangeRates(List<ExchangeRate> exchangeRateList) {
        Map<Currency, BigDecimal> ratesMap = exchangeRateList.stream()
                .collect(Collectors.
                        toMap(rate -> rate.getExchangeRateId().getTargetCurrency(), ExchangeRate::getRate));
        if (!exchangeRateList.isEmpty()) {
            ExchangeRate exchangeRate = exchangeRateList.get(0);
            ExchangeRates exchangeRates = new ExchangeRates(
                    exchangeRate.getTimestamp(),
                    serviceProperties.getBaseCurrency(),
                    exchangeRate.getExchangeRateId().getDate(),
                    new TreeMap<>(ratesMap)); // TreeMap, so the rate's map will be sorted
            return Optional.of(exchangeRates);
        }
        return Optional.empty();
    }

    public List<ExchangeRate> mapToEntities(ExchangeRates exchangeRates) {
        return exchangeRates.rates().entrySet().stream().map(entry -> {
            ExchangeRateId rateId = new ExchangeRateId(exchangeRates.date(), entry.getKey());
            return new ExchangeRate(rateId, exchangeRates.timestamp(), entry.getValue(), BigInteger.ZERO);
        }).toList();
    }
}
