package pl.rationalworks.exchangeratetest.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
@ConfigurationProperties(prefix = "providers.fixer")
@Getter
@Setter
public class FixerProviderProperties {
    URI latestPath;
    String apiKey;

}
