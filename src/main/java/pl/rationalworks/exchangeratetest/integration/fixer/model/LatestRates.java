package pl.rationalworks.exchangeratetest.integration.fixer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class LatestRates extends FixerResponse {
    private final Instant timestamp;
    private final Currency base;
    private final LocalDate date;
    private final Map<Currency, BigDecimal> rates;

    @JsonCreator
    public LatestRates(@JsonProperty("success") boolean success, @JsonProperty("timestamp") Instant timestamp,
                       @JsonProperty("base") Currency base, @JsonProperty("date") LocalDate date,
                       @JsonProperty("rates") Map<Currency, BigDecimal> rates) {
        super(success);
        this.timestamp = timestamp;
        this.base = base;
        this.date = date;
        this.rates = rates;
    }
}
