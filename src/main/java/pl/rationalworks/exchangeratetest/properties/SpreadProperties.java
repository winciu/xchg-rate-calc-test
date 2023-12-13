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
    @Value("${service.spread.base-currency.value}")
    private BigDecimal baseCurrencySpread;
    @Value("${service.spread.base-currency.code}")
    private Currency baseCurrency;
    private Map<Currency, BigDecimal> mapping;

    public BigDecimal obtainCurrencySpread(Currency currency) {
        if (baseCurrency.equals(currency)) {
            return baseCurrencySpread;
        }
        return mapping.getOrDefault(currency, defaultSpread);
    }

}
