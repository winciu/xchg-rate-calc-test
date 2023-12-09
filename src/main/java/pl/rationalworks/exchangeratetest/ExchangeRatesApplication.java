package pl.rationalworks.exchangeratetest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("pl.rationalworks.exchangeratetest.properties")
public class ExchangeRatesApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExchangeRatesApplication.class, args);
	}

}
