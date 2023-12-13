package pl.rationalworks.exchangeratetest.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "exchange_rates", indexes = @Index(name = "xchg_date_idx", columnList = "date"))
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ExchangeRate {

    @EmbeddedId
    private ExchangeRateId exchangeRateId;

    /**
     * exact date and time (UNIX time stamp) the given rates were collected
     */
    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;

    @Column(name = "rate", nullable = false, precision = 12, scale = 6)
    private BigDecimal rate;

}
