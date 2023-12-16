package pl.rationalworks.exchangeratetest.integration.fixer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import pl.rationalworks.exchangeratetest.model.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class LatestRates extends FixerResponse {
    /**
     * Returns the exact date and time (UNIX time stamp) the given rates were collected.
     */
    private final Instant timestamp;
    /**
     * Returns the three-letter currency code of the base currency used for this request.
     */
    private final Currency base;
    /**
     * Returns the date the given exchange rate data was collected
     */
    private final LocalDate date;
    /**
     * Returns exchange rate data for the currencies you have requested.
     */
    private final Map<Currency, BigDecimal> rates;

    @JsonCreator
    public LatestRates(@JsonProperty("success") boolean success,
                       @JsonProperty("timestamp") Instant timestamp,
                       @JsonProperty("base") Currency base,
                       @JsonProperty("date") LocalDate date,
                       @JsonProperty("rates") Map<Currency, BigDecimal> rates) {
        super(success);
        this.timestamp = timestamp;
        this.base = base;
        this.date = date;
        this.rates = rates;
    }
}
