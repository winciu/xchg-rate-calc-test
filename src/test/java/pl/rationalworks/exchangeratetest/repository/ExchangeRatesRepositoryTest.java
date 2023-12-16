package pl.rationalworks.exchangeratetest.repository;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;
import pl.rationalworks.exchangeratetest.model.ExchangeRate;
import pl.rationalworks.exchangeratetest.model.ExchangeRateId;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import pl.rationalworks.exchangeratetest.model.Currency;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.math.BigDecimal.valueOf;
import static java.time.LocalDate.ofInstant;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DataJpaTest
public class ExchangeRatesRepositoryTest {

    @Autowired
    TestEntityManager entityManager;
    @Autowired
    private ExchangeRatesRepository repository;

    private static Stream<Arguments> shouldSaveAllExchangeRates() {
        return Stream.of(
                Arguments.of(Currency.getInstance("MUR"),
                        Instant.now().minus(1, ChronoUnit.DAYS),
                        Map.of(
                                Currency.getInstance("MOP"), valueOf(8.677294),
                                Currency.getInstance("MRU"), valueOf(42.667066),
                                Currency.getInstance("MUR"), valueOf(47.522284),
                                Currency.getInstance("MVR"), valueOf(16.537719),
                                Currency.getInstance("MWK"), valueOf(1815.098742),
                                Currency.getInstance("MXN"), valueOf(18.689897)
                        )),
                Arguments.of(Currency.getInstance("USD"),
                        Instant.now().minus(2, ChronoUnit.DAYS),
                        Map.of(
                                Currency.getInstance("UAH"), valueOf(39.607273),
                                Currency.getInstance("UGX"), valueOf(4064.997183),
                                Currency.getInstance("USD"), valueOf(1.077349),
                                Currency.getInstance("UYU"), valueOf(42.139971),
                                Currency.getInstance("UZS"), valueOf(13275.990801),
                                Currency.getInstance("VEF"), valueOf(3830760.985659)
                        )));
    }

    @ParameterizedTest
    @MethodSource
    void shouldSaveAllExchangeRates(Currency itemToCheck, Instant timestamp, Map<Currency, BigDecimal> rates) {
        List<ExchangeRate> exchangeRates = rates.entrySet().stream().map(entry -> {
            ExchangeRateId exchangeRateId = new ExchangeRateId(ofInstant(timestamp, ZoneId.systemDefault()), entry.getKey());
            return new ExchangeRate(exchangeRateId, timestamp, entry.getValue(), BigInteger.ZERO);
        }).toList();

        log.info("Start saving {} exchange rates", exchangeRates.size());
        repository.saveAll(exchangeRates);
        log.info("Exchange rates saved.");

        ExchangeRateId exchangeRateId = new ExchangeRateId(ofInstant(timestamp, ZoneId.systemDefault()), itemToCheck);
        ExchangeRate exchangeRate = entityManager.find(ExchangeRate.class, exchangeRateId);
        assertEquals(rates.get(itemToCheck), exchangeRate.getRate());
        assertEquals(timestamp, exchangeRate.getTimestamp());
    }

    @Test
    @Sql("classpath:sql/latest_rates_1.sql")
    @Sql("classpath:sql/latest_rates_2.sql")
    void shouldReturnCorrectExchangeRateByDateForGivenCurrencies() {
        Optional<ExchangeRate> rate = repository.findExchangeRateByExchangeRateId_DateAndExchangeRateId_TargetCurrency(LocalDate.parse("2023-12-09"), Currency.getInstance("CZK"));
        rate.ifPresentOrElse(r -> assertThat(r.getRate(), comparesEqualTo(valueOf(24.39507))), () -> fail("Rate not found"));

        Optional<ExchangeRate> rate2 = repository.findExchangeRateByExchangeRateId_DateAndExchangeRateId_TargetCurrency(LocalDate.parse("2023-12-12"), Currency.getInstance("COP"));
        rate2.ifPresentOrElse(r -> assertThat(r.getRate(), comparesEqualTo(valueOf(4307.712239))), () -> fail("Rate not found"));
    }

    @Test
    @Sql("classpath:sql/latest_rates_1.sql")
    @Sql("classpath:sql/latest_rates_2.sql")
    void shouldReturnLatestExchangeRateForGivenCurrencies() {
        Optional<ExchangeRate> rate = repository.findFirstByExchangeRateId_TargetCurrencyOrderByExchangeRateId_DateDesc(Currency.getInstance("CUP"));
        rate.ifPresentOrElse(r -> assertThat(r.getRate(), comparesEqualTo(valueOf(28.609832))), () -> fail("Rate not found"));
    }

    @Test
    @Sql("classpath:sql/latest_rates_1.sql")
    void shouldReturnEmptyValueForNonExistingCurrency() {
        Optional<ExchangeRate> rate = repository.findExchangeRateByExchangeRateId_DateAndExchangeRateId_TargetCurrency(LocalDate.parse("2023-12-10"), Currency.getInstance("COP"));
        assertFalse(rate.isPresent());

        Optional<ExchangeRate> rate2 = repository.findFirstByExchangeRateId_TargetCurrencyOrderByExchangeRateId_DateDesc(Currency.getInstance("USD"));
        assertFalse(rate2.isPresent());
    }

    @Test
    @Sql("classpath:sql/latest_rates_1.sql")
    void requestCounterShouldBeUpdated() {
        List<ExchangeRateId> exchangeRateIds = List.of(
                new ExchangeRateId(LocalDate.parse("2023-12-09"), Currency.getInstance("CLP")),
                new ExchangeRateId(LocalDate.parse("2023-12-09"), Currency.getInstance("CNY")),
                new ExchangeRateId(LocalDate.parse("2023-12-09"), Currency.getInstance("CZK"))
        );
        exchangeRateIds.forEach(id -> repository.updateRequestCounter(id));

        exchangeRateIds.forEach(id -> {
            ExchangeRate exchangeRate = entityManager.find(ExchangeRate.class, id);
            MatcherAssert.assertThat(exchangeRate.getRequestCounter(), equalTo(BigInteger.ONE));
        });
    }

}
