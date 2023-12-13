package pl.rationalworks.exchangeratetest.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Currency;

@ConfigurationProperties(prefix = "service")
@Getter
@Setter
public class ServiceProperties {
    private Currency baseCurrency;
}
