package pl.rationalworks.exchangeratetest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;
import java.util.Currency;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
@Embeddable
@Getter
public class ExchangeRateId implements Serializable {
    public static final String PROVIDER_NAME = "Fixer";

    /**
     * exact date and time (UNIX time stamp) the given rates were collected
     */
    @Column(name = "timestamp", nullable = false, updatable = false)
    private final Instant timestamp;
    @Column(name = "provider", nullable = false, updatable = false)
    private final String providerName;
    @Column(name = "base_currency", nullable = false, updatable = false)
    private final Currency baseCurrency;
    @Column(name = "target_currency", nullable = false, updatable = false)
    private final Currency targetCurrency;

    public ExchangeRateId() {
        this(null, PROVIDER_NAME, null, null);
    }
}
