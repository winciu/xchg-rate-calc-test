package pl.rationalworks.exchangeratetest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestExchangeRatesApplication {

	public static void main(String[] args) {
		SpringApplication.from(ExchangeRatesApplication::main).with(TestExchangeRatesApplication.class).run(args);
	}

}
