package pl.rationalworks.exchangeratetest.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "exchange_rates")
//@IdClass(ExchangeRateId.class)
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ExchangeRate {

    @EmbeddedId
    private ExchangeRateId exchangeRateId;

    //    @Id
//    @Column(name = "provider", nullable = false)
//    private String providerName;
//    @Id
//    @Column(name = "base_currency", nullable = false)
//    private Currency baseCurrency;
//    @Id
//    @Column(name = "target_currency", nullable = false)
//    private Currency targetCurrency;

    /**
     * the date the given exchange rate data was collected
     */
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "rate", nullable = false)
    private BigDecimal rate;

}
