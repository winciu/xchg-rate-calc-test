package pl.rationalworks.exchangeratetest.schedule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.rationalworks.exchangeratetest.integration.fixer.FixerExchangeRatesProvider;
import pl.rationalworks.exchangeratetest.integration.fixer.FixerRatesProviderException;
import pl.rationalworks.exchangeratetest.integration.fixer.model.LatestRates;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRateFetchingSchedulerTest {

    @InjectMocks
    private ExchangeRateFetchingScheduler scheduler;

    @Mock
    private FixerExchangeRatesProvider provider;

    @Test
    void shouldCallRatesProvider() throws FixerRatesProviderException {
        LatestRates rates = new LatestRates(true, Instant.now(), Currency.getInstance("EUR"),
                LocalDate.now(), Map.of(Currency.getInstance("USD"), BigDecimal.valueOf(3.954532)));
        when(provider.provideExchangeRates()).thenReturn(rates);

        scheduler.fetchFixerExchangeRates();

        verify(provider, atMostOnce()).provideExchangeRates();
    }
}