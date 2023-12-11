package pl.rationalworks.exchangeratetest.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.rationalworks.exchangeratetest.integration.fixer.FixerExchangeRatesProvider;
import pl.rationalworks.exchangeratetest.integration.fixer.FixerRatesProviderException;
import pl.rationalworks.exchangeratetest.integration.fixer.model.LatestRates;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduling.fixer.enabled", matchIfMissing = true)
public class ExchangeRateFetchingScheduler {

    private final FixerExchangeRatesProvider ratesProvider;

    @Scheduled(cron = " ${scheduling.fixer.cron}", zone = "${scheduling.fixer.timezone}")
    public void fetchFixerExchangeRates() {
        log.info("Start fetching Fixer exchange rates ....");
        LatestRates rates = null;
        try {
            rates = ratesProvider.provideExchangeRates();
        } catch (FixerRatesProviderException e) {
            log.error("Cannot fetch rates.", e);
            return;
        }
        log.info("Rates fetched. Timestamp: {}", rates.getTimestamp().toString());

    }
}
