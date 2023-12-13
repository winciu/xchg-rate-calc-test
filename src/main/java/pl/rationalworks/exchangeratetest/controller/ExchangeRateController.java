package pl.rationalworks.exchangeratetest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.rationalworks.exchangeratetest.model.ExchangeOperationRepresentation;
import pl.rationalworks.exchangeratetest.service.ExchangeRateService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Optional;

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
}
