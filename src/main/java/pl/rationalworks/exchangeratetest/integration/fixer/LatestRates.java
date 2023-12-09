package pl.rationalworks.exchangeratetest.integration.fixer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LatestRates(Instant timestamp, Currency base, LocalDate date, Map<Currency, BigDecimal> rates) {
}
