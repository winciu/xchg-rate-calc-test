package pl.rationalworks.exchangeratetest.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;

@ConfigurationProperties(prefix = "service.spread")
@Getter
@Setter
public class SpreadProperties {
    @Value("${service.spread.default}")
    private BigDecimal defaultSpread;
    private BigDecimal baseCurrency;
    private Map<Currency, BigDecimal> mapping;
}
