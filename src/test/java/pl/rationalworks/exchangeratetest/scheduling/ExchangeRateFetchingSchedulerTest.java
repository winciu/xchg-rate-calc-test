package pl.rationalworks.exchangeratetest.scheduling;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.rationalworks.exchangeratetest.integration.fixer.FixerExchangeRatesProvider;
import pl.rationalworks.exchangeratetest.integration.fixer.FixerRatesProviderException;
import pl.rationalworks.exchangeratetest.model.dto.ExchangeRates;
import pl.rationalworks.exchangeratetest.service.ExchangeRateService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRateFetchingSchedulerTest {

    @InjectMocks
    private ExchangeRateFetchingScheduler scheduler;

    @Mock
    private FixerExchangeRatesProvider provider;
    @Mock
    private ExchangeRateService service;

    @Test
    void shouldCallRatesProvider() throws FixerRatesProviderException {
        ExchangeRates rates = new ExchangeRates(Instant.now(), Currency.getInstance("EUR"),
                LocalDate.now(), Map.of(Currency.getInstance("USD"), BigDecimal.valueOf(3.954532)));
        when(service.fetchRatesFromFixer()).thenReturn(Optional.of(rates));

        scheduler.fetchFixerExchangeRates();

        verify(provider, atMostOnce()).provideExchangeRates();
        verify(service, atMostOnce()).saveOrUpdateRates(Mockito.assertArg(exchangeRates -> {
            Assertions.assertEquals(rates.date(), exchangeRates.date());
        }));
    }
}