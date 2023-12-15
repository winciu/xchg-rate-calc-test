package pl.rationalworks.exchangeratetest.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.rationalworks.exchangeratetest.model.ExchangeRate;
import pl.rationalworks.exchangeratetest.model.ExchangeRateId;
import pl.rationalworks.exchangeratetest.model.dto.ExchangeRates;
import pl.rationalworks.exchangeratetest.repository.ExchangeRatesRepository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @InjectMocks
    private ExchangeRateService service;
    @Mock
    private ExchangeRatesRepository repository;

    @Test
    void shouldSaveAllGivenExchangeRatesInCaseTheyNotExist() {
        Map<Currency, BigDecimal> rates = Map.of(
                Currency.getInstance("XOF"), BigDecimal.valueOf(2.958057),
                Currency.getInstance("XDR"), BigDecimal.valueOf(655.956545),
                Currency.getInstance("XCD"), BigDecimal.valueOf(0.046825),
                Currency.getInstance("XAU"), BigDecimal.valueOf(0.000537),
                Currency.getInstance("XAG"), BigDecimal.valueOf(2.911591),
                Currency.getInstance("XAF"), BigDecimal.valueOf(0.811499)
        );
        LocalDate date = LocalDate.now();
        Currency base = Currency.getInstance("EUR");
        ExchangeRates latestRates = new ExchangeRates(Instant.now(), base, date, rates);

        when(repository.findAllByExchangeRateId_Date(eq(date))).thenReturn(Optional.empty());

        service.saveOrUpdateRates(latestRates);

        verify(repository).saveAll(assertArg(it -> {
            List<String> currencies = stream(it.spliterator(), false)
                    .map(rate -> rate.getExchangeRateId().getTargetCurrency())
                    .map(Currency::toString)
                    .toList();

            assertThat(currencies, containsInAnyOrder(rates.keySet().stream().map(Currency::toString).toArray()));
            assertThat(currencies, hasSize(rates.size()));
        }));
    }

    @Test
    void shouldUpdateAllRelevantExchangeRates() {
        LocalDate date = LocalDate.now();
        Instant timestamp = Instant.now();

        List<ExchangeRate> exchangeRateList = List.of(
                new ExchangeRate(new ExchangeRateId(date, Currency.getInstance("XOF")), timestamp, BigDecimal.valueOf(2.958057), BigInteger.ZERO),
                new ExchangeRate(new ExchangeRateId(date, Currency.getInstance("XDR")), timestamp, BigDecimal.valueOf(655.956545), BigInteger.ZERO),
                new ExchangeRate(new ExchangeRateId(date, Currency.getInstance("XCD")), timestamp, BigDecimal.valueOf(0.046825), BigInteger.ZERO),
                new ExchangeRate(new ExchangeRateId(date, Currency.getInstance("XAU")), timestamp, BigDecimal.valueOf(0.000537), BigInteger.ZERO),
                new ExchangeRate(new ExchangeRateId(date, Currency.getInstance("XAG")), timestamp, BigDecimal.valueOf(2.911591), BigInteger.ZERO),
                new ExchangeRate(new ExchangeRateId(date, Currency.getInstance("XAF")), timestamp, BigDecimal.valueOf(0.811499), BigInteger.ZERO)
        );

        when(repository.findAllByExchangeRateId_Date(eq(date))).thenReturn(Optional.of(exchangeRateList));


        Currency base = Currency.getInstance("EUR");
        Map<Currency, BigDecimal> rates = Map.of(
                Currency.getInstance("XOF"), BigDecimal.valueOf(3.958057),
                Currency.getInstance("XDR"), BigDecimal.valueOf(656.956545),
                Currency.getInstance("XCD"), BigDecimal.valueOf(1.046825),
                Currency.getInstance("XAU"), BigDecimal.valueOf(1.000537),
                Currency.getInstance("XAG"), BigDecimal.valueOf(3.911591),
                Currency.getInstance("XAF"), BigDecimal.valueOf(1.811499)
        );
        ExchangeRates latestRates = new ExchangeRates(Instant.now(), base, date, rates);
        service.saveOrUpdateRates(latestRates);

        Map<ExchangeRateId, ExchangeRate> dbListAsMap = exchangeRateList.stream().collect(Collectors.toMap(ExchangeRate::getExchangeRateId, Function.identity()));

        verify(repository).saveAll(assertArg(dbRates -> {
            dbRates.forEach(dbRate -> {
                assertThat(dbRate.getRequestCounter(), is(BigInteger.ZERO));
                ExchangeRate exchangeRate = dbListAsMap.get(dbRate.getExchangeRateId());
                assertEquals(dbRate.getExchangeRateId(), exchangeRate.getExchangeRateId());
                assertEquals(dbRate.getRate(), exchangeRate.getRate());
            });
        }));

    }

}