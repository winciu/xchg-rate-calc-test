package pl.rationalworks.exchangeratetest.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.rationalworks.exchangeratetest.model.Currency;
import pl.rationalworks.exchangeratetest.model.ExchangeRate;
import pl.rationalworks.exchangeratetest.model.ExchangeRateId;
import pl.rationalworks.exchangeratetest.model.dto.ExchangeRates;
import pl.rationalworks.exchangeratetest.model.mapper.ExchangeRateToDtoMapper;
import pl.rationalworks.exchangeratetest.properties.SpreadProperties;
import pl.rationalworks.exchangeratetest.repository.ExchangeRatesRepository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @InjectMocks
    private ExchangeRateService service;
    @Mock
    private ExchangeRatesRepository repository;
    @Mock
    private ExchangeRateToDtoMapper mapper;
    @Mock
    private SpreadProperties spreadProperties;
    @Spy
    private ExchangeRateCalculator rateCalculator;

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
        Instant timestamp = Instant.now();
        ExchangeRates latestRates = new ExchangeRates(timestamp, base, date, rates);

        when(repository.findAllByExchangeRateId_Date(eq(date))).thenReturn(List.of());
        List<ExchangeRate> exchangeRates = rates.entrySet().stream()
                .map(entry -> new ExchangeRate(
                        new ExchangeRateId(date, entry.getKey()), timestamp, entry.getValue(), BigInteger.ZERO))
                .toList();
        when(mapper.mapToEntities(latestRates)).thenReturn(exchangeRates);

        service.saveOrUpdateRates(latestRates);

        verify(repository).saveAll(exchangeRates);
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

        when(repository.findAllByExchangeRateId_Date(eq(date))).thenReturn(exchangeRateList);


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

        Map<ExchangeRateId, ExchangeRate> dbListAsMap = exchangeRateList.stream()
                .collect(Collectors.toMap(ExchangeRate::getExchangeRateId, Function.identity()));

        verify(repository).saveAll(assertArg(dbRates -> {
            dbRates.forEach(dbRate -> {
                assertThat(dbRate.getRequestCounter(), is(BigInteger.ZERO));
                ExchangeRate exchangeRate = dbListAsMap.get(dbRate.getExchangeRateId());
                assertEquals(dbRate.getExchangeRateId(), exchangeRate.getExchangeRateId());
                assertEquals(dbRate.getRate(), exchangeRate.getRate());
            });
        }));
    }

    @Test
    void shouldCalculateExchangeRateProperlyWhenDateIsNotSpecified() {
        shouldCalculateExchangeRateProperly(null);
    }

    @Test
    void shouldCalculateExchangeRateProperlyWhenDateIsSpecified() {
        shouldCalculateExchangeRateProperly(LocalDate.now());
    }

    private void shouldCalculateExchangeRateProperly(LocalDate date) {
        Currency fromCurrency = Currency.getInstance("USD");
        Currency toCurrency = Currency.getInstance("PLN");
        BigDecimal rateFrom = BigDecimal.valueOf(1.079616);
        BigDecimal rateTo = BigDecimal.valueOf(4.328805);
        Instant timestamp = Instant.now();

        ExchangeRateId rateIdFrom = new ExchangeRateId(date, fromCurrency);
        ExchangeRate exchangeRateFrom = new ExchangeRate(rateIdFrom, timestamp, rateFrom, BigInteger.ZERO);
        ExchangeRateId rateIdTo = new ExchangeRateId(date, toCurrency);
        ExchangeRate exchangeRateTo = new ExchangeRate(rateIdTo, timestamp, rateTo, BigInteger.ZERO);

        BigDecimal defaultSpread = BigDecimal.valueOf(2.75);
        when(spreadProperties.obtainCurrencySpread(any(Currency.class))).thenReturn(defaultSpread);

        if (Objects.isNull(date)) {
            when(repository.findFirstByExchangeRateId_TargetCurrencyOrderByExchangeRateId_DateDesc(fromCurrency))
                    .thenReturn(Optional.of(exchangeRateFrom));
            when(repository.findFirstByExchangeRateId_TargetCurrencyOrderByExchangeRateId_DateDesc(toCurrency))
                    .thenReturn(Optional.of(exchangeRateTo));
        } else {
            when(repository.findExchangeRateByExchangeRateId_DateAndExchangeRateId_TargetCurrency(date, fromCurrency))
                    .thenReturn(Optional.of(exchangeRateFrom));
            when(repository.findExchangeRateByExchangeRateId_DateAndExchangeRateId_TargetCurrency(date, toCurrency))
                    .thenReturn(Optional.of(exchangeRateTo));
        }

        Optional<BigDecimal> calculatedExchangeRate = service.calculateExchangeRate(fromCurrency, toCurrency, date);

        //verify that request counters for both currencies were updated
        verify(repository, times(1)).updateRequestCounter(rateIdFrom);
        verify(repository, times(1)).updateRequestCounter(rateIdTo);
        verify(rateCalculator, times(1)).calculateExchangeRate(rateFrom, rateTo, defaultSpread, defaultSpread);

        assertTrue(calculatedExchangeRate.isPresent());
        assertEquals(new BigDecimal("3.8993149994998220400"), calculatedExchangeRate.get());
    }

    @Test
    void shouldNotReturnCalculatedExchangeRateWhenOneOfTheRateIsMissingInDatabase() {
        Currency fromCurrency = Currency.getInstance("USD");
        Currency toCurrency = Currency.getInstance("PLN");
        LocalDate date = LocalDate.now();
        BigDecimal rateFrom = BigDecimal.valueOf(1.079616);
        Instant timestamp = Instant.now();

        ExchangeRateId rateIdFrom = new ExchangeRateId(date, fromCurrency);
        ExchangeRate exchangeRateFrom = new ExchangeRate(rateIdFrom, timestamp, rateFrom, BigInteger.ZERO);

        when(repository.findExchangeRateByExchangeRateId_DateAndExchangeRateId_TargetCurrency(date, fromCurrency))
                .thenReturn(Optional.of(exchangeRateFrom));
        when(repository.findExchangeRateByExchangeRateId_DateAndExchangeRateId_TargetCurrency(date, toCurrency))
                .thenReturn(Optional.empty());

        Optional<BigDecimal> calculatedExchangeRate = service.calculateExchangeRate(fromCurrency, toCurrency, date);

        assertFalse(calculatedExchangeRate.isPresent());

        //verify that request counter's update method was invoked only once (for the rate that is present in DB)
        verify(repository, times(1)).updateRequestCounter(rateIdFrom);
        verifyNoMoreInteractions(repository);
    }

}