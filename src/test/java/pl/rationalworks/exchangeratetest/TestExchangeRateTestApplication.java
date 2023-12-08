package pl.rationalworks.exchangeratetest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestExchangeRateTestApplication {

	public static void main(String[] args) {
		SpringApplication.from(ExchangeRateTestApplication::main).with(TestExchangeRateTestApplication.class).run(args);
	}

}
