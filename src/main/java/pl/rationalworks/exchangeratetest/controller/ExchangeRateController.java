package pl.rationalworks.exchangeratetest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.rationalworks.exchangeratetest.model.Currency;
import pl.rationalworks.exchangeratetest.model.dto.ExchangeOperationRepresentation;
import pl.rationalworks.exchangeratetest.model.dto.ExchangeRates;
import pl.rationalworks.exchangeratetest.service.ExchangeRateService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/exchange")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @Operation(summary = "Calculate exchange rate including defined currency spread")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the book",
                    content = {@Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = "{\"from\":\"USD\",\"to\":\"PLN\",\"exchange\":3.8648371093836001875}")
                    }, schema = @Schema(implementation = ExchangeOperationRepresentation.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid parameter supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "There are no exchange rates for the specified date",
                    content = @Content)})
    @GetMapping
    public ResponseEntity<ExchangeOperationRepresentation> calculateExchangeRate(@RequestParam Currency from,
                                                                                 @RequestParam Currency to,
                                                                                 @RequestParam(required = false) LocalDate date) {
        Optional<BigDecimal> calculateExchangeRate = exchangeRateService.calculateExchangeRate(from, to, date);
        return calculateExchangeRate
                .map(rate -> ResponseEntity.ok(new ExchangeOperationRepresentation(from, to, rate)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Fetches latest exchange rates from the rate's provider and save them for further use")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rates saved without issues",
                    content = {@Content(mediaType = "application/json", examples = {
                            @ExampleObject(value = "{\"timestamp\":1702848243000,\"base\":\"EUR\",\"date\":\"2023-12-17\",\"rates\":{\"AED\":4.003432,\"AFN\":76.558366,\"ALL\":103.916341,...,\"ZWL\":351.002878}}")
                    }, schema = @Schema(implementation = ExchangeRates.class))}),
            @ApiResponse(responseCode = "204", description = "No rates were loaded due to an error",
                    content = @Content)})
    @PutMapping
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
