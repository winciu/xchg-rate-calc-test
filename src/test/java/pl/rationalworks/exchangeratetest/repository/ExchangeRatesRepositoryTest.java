package pl.rationalworks.exchangeratetest.repository;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import pl.rationalworks.exchangeratetest.model.ExchangeRate;
import pl.rationalworks.exchangeratetest.model.ExchangeRateId;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.time.LocalDate.ofInstant;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class ExchangeRatesRepositoryTest {

    @Autowired
    TestEntityManager entityManager;
    @Autowired
    private ExchangeRatesRepository repository;

    private static Stream<Arguments> shouldSaveAllExchangeRates() {
        return Stream.of(
                Arguments.of(1, Currency.getInstance("MUR"),
                        Instant.now().minus(1, ChronoUnit.DAYS),
                        Map.of(
                                Currency.getInstance("MOP"), BigDecimal.valueOf(8.677294),
                                Currency.getInstance("MRU"), BigDecimal.valueOf(42.667066),
                                Currency.getInstance("MUR"), BigDecimal.valueOf(47.522284),
                                Currency.getInstance("MVR"), BigDecimal.valueOf(16.537719),
                                Currency.getInstance("MWK"), BigDecimal.valueOf(1815.098742),
                                Currency.getInstance("MXN"), BigDecimal.valueOf(18.689897)
                        )),
                Arguments.of(2, Currency.getInstance("USD"),
                        Instant.now().minus(2, ChronoUnit.DAYS),
                        Map.of(
                                Currency.getInstance("UAH"), BigDecimal.valueOf(39.607273),
                                Currency.getInstance("UGX"), BigDecimal.valueOf(4064.997183),
                                Currency.getInstance("USD"), BigDecimal.valueOf(1.077349),
                                Currency.getInstance("UYU"), BigDecimal.valueOf(42.139971),
                                Currency.getInstance("UZS"), BigDecimal.valueOf(13275.990801),
                                Currency.getInstance("VEF"), BigDecimal.valueOf(3830760.985659)
                        )));
    }

    @ParameterizedTest
    @MethodSource
    void shouldSaveAllExchangeRates(int daysBack, Currency itemToCheck, Instant timestamp, Map<Currency, BigDecimal> rates) {
        Currency baseCurrency = Currency.getInstance("EUR");
        String providerName = "name";
        List<ExchangeRate> exchangeRates = rates.entrySet().stream().map(entry -> {
            ExchangeRateId exchangeRateId = new ExchangeRateId(timestamp, providerName, baseCurrency, entry.getKey());
            return new ExchangeRate(exchangeRateId, ofInstant(timestamp, ZoneId.systemDefault()), entry.getValue());
        }).toList();

        repository.saveAll(exchangeRates);

        ExchangeRateId exchangeRateId = new ExchangeRateId(timestamp, providerName, baseCurrency, itemToCheck);
        ExchangeRate exchangeRate = entityManager.find(ExchangeRate.class, exchangeRateId);
        assertEquals(rates.get(itemToCheck), exchangeRate.getRate());
        Instant instant = Instant.now().minus(daysBack, ChronoUnit.DAYS);
        assertEquals(ofInstant(instant, ZoneId.systemDefault()), exchangeRate.getDate());
    }

}
