package pl.rationalworks.exchangeratetest.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.rationalworks.exchangeratetest.model.dto.ExchangeOperationRepresentation;
import pl.rationalworks.exchangeratetest.model.dto.ExchangeRates;
import pl.rationalworks.exchangeratetest.service.ExchangeRateService;

import java.math.BigDecimal;
import java.time.LocalDate;
import pl.rationalworks.exchangeratetest.model.Currency;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @GetMapping("/exchange")
    public ResponseEntity<ExchangeOperationRepresentation> calculateExchangeRate(@RequestParam Currency from,
                                                                                 @RequestParam Currency to,
                                                                                 @RequestParam(required = false) LocalDate date) {
        Optional<BigDecimal> calculateExchangeRate = exchangeRateService.calculateExchangeRate(from, to, date);
        return calculateExchangeRate
                .map(rate -> ResponseEntity.ok(new ExchangeOperationRepresentation(from, to, rate)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/exchange")
    public ResponseEntity<ExchangeRates> fetchLatestRates() {
        Optional<ExchangeRates> fixerRates = exchangeRateService.fetchRatesFromFixer();
        if (fixerRates.isPresent()) {
            ExchangeRates exchangeRates = fixerRates.get();
            exchangeRateService.saveOrUpdateRates(exchangeRates);
            Optional<ExchangeRates> latestRates = exchangeRateService.obtainRatesByDate(exchangeRates.date());
            return latestRates.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
        }
        return ResponseEntity.noContent().build();
    }
}
