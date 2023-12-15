package pl.rationalworks.exchangeratetest.scheduling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.rationalworks.exchangeratetest.service.ExchangeRateService;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "service.scheduling.fixer.enabled", matchIfMissing = true)
public class ExchangeRateFetchingScheduler {

    private final ExchangeRateService service;

    @Scheduled(cron = " ${service.scheduling.fixer.cron}", zone = "${service.scheduling.fixer.timezone}")
    public void fetchFixerExchangeRates() {
        log.info("Scheduled task started: Fixer latest rates download ...");
        service.fetchRatesFromFixer().ifPresent(service::saveOrUpdateRates);
        log.info("Scheduled task finished: Fixer latest rates downloaded and saved.");
    }
}
