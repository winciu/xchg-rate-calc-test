package pl.rationalworks.exchangeratetest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//@ConfigurationPropertiesScan("pl.rationalworks.exchangeratetest.properties")
@EnableScheduling
public class ExchangeRatesApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExchangeRatesApplication.class, args);
	}

}
