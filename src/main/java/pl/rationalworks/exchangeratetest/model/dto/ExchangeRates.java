package pl.rationalworks.exchangeratetest.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import pl.rationalworks.exchangeratetest.model.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

public record ExchangeRates(@JsonFormat(shape = JsonFormat.Shape.NUMBER_INT) Instant timestamp,
                            Currency base,
                            @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING) LocalDate date,
                            Map<Currency, BigDecimal> rates) {
}
