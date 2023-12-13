package pl.rationalworks.exchangeratetest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Currency;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
@Embeddable
@Getter
public class ExchangeRateId implements Serializable {

    /**
     * the date the given exchange rate data was collected
     */
    @Column(name = "date", nullable = false)
    private final LocalDate date;

    @Column(name = "target_currency", nullable = false, updatable = false)
    private final Currency targetCurrency;

    public ExchangeRateId() {
        this(null, null);
    }
}
