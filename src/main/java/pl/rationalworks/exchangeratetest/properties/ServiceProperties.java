package pl.rationalworks.exchangeratetest.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.stereotype.Component;
import pl.rationalworks.exchangeratetest.model.Currency;

@Component
@ConfigurationProperties(prefix = "service")
@Getter
@Setter
public class ServiceProperties {
    private Currency baseCurrency;
}
