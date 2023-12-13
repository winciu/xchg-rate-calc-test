package pl.rationalworks.exchangeratetest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.rationalworks.exchangeratetest.model.ExchangeOperationRepresentation;
import pl.rationalworks.exchangeratetest.service.ExchangeRateService;

import java.time.LocalDate;
import java.util.Currency;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @GetMapping("/exchange")
    public ResponseEntity<ExchangeOperationRepresentation> findRoute(@RequestParam Currency from, @RequestParam Currency to,
                                                                     @RequestParam LocalDate date) {
        ExchangeOperationRepresentation result = exchangeRateService.calculateExchangeRate(from, to, date);
        return ResponseEntity.ok(result);
    }
}
