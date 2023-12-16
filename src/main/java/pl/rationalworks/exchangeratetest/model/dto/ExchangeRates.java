package pl.rationalworks.exchangeratetest.model.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import pl.rationalworks.exchangeratetest.model.Currency;
import java.util.Map;

public record ExchangeRates(Instant timestamp, Currency base, LocalDate date, Map<Currency, BigDecimal> rates) {
}
