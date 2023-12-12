package pl.rationalworks.exchangeratetest.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.rationalworks.exchangeratetest.integration.fixer.model.LatestRates;
import pl.rationalworks.exchangeratetest.repository.ExchangeRatesRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @InjectMocks
    private ExchangeRateService service;
    @Mock
    private ExchangeRatesRepository repository;

    @Test
    void shouldSaveAllGivenExchangeRates() {
        Currency base = Currency.getInstance("EUR");
        Map<Currency, BigDecimal> rates = Map.of(
                Currency.getInstance("XOF"), BigDecimal.valueOf(2.958057),
                Currency.getInstance("XDR"), BigDecimal.valueOf(655.956545),
                Currency.getInstance("XCD"), BigDecimal.valueOf(0.046825),
                Currency.getInstance("XAU"), BigDecimal.valueOf(0.000537),
                Currency.getInstance("XAG"), BigDecimal.valueOf(2.911591),
                Currency.getInstance("XAF"), BigDecimal.valueOf(0.811499),
                Currency.getInstance("WST"), BigDecimal.valueOf(655.956545)
        );
        LatestRates latestRates = new LatestRates(true, Instant.now(), base, LocalDate.now(), rates);

        service.saveFixerRates(latestRates);

        verify(repository).saveAll(assertArg(it -> {
            List<String> currencies = stream(it.spliterator(), false)
                    .map(rate -> rate.getExchangeRateId().getTargetCurrency())
                    .map(Currency::toString)
                    .toList();

            assertThat(currencies, containsInAnyOrder(rates.keySet().stream().map(Currency::toString).toArray()));
            assertThat(currencies, hasSize(rates.size()));
        }));
    }

}