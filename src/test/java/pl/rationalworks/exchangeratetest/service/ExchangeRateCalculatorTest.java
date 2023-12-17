package pl.rationalworks.exchangeratetest.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static java.math.BigDecimal.valueOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ExchangeRateCalculatorTest {

    private static Stream<Arguments> shouldCalculateExchangeRateCorrectly() {
        return Stream.of(
                Arguments.of(valueOf(3.7), valueOf(0.45656), valueOf(4), valueOf(1), new BigDecimal("7.77991939723147008")),
                Arguments.of(valueOf(4.338897), valueOf(1), valueOf(4), valueOf(0), valueOf(4.16534112)),
                Arguments.of(valueOf(4.314691), valueOf(0.000025801478), valueOf(4), valueOf(0), new BigDecimal("160537.445180466048"))
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldCalculateExchangeRateCorrectly(BigDecimal toCurrencyRate,
                                              BigDecimal fromCurrencyRate,
                                              BigDecimal toCurrencySpread,
                                              BigDecimal fromCurrencySpread,
                                              BigDecimal expectedResult) {
        ExchangeRateCalculator exchangeRateCalculator = new ExchangeRateCalculator();
        BigDecimal actual = exchangeRateCalculator.calculateExchangeRate(fromCurrencyRate, toCurrencyRate, fromCurrencySpread, toCurrencySpread);
        assertThat(actual, equalTo(expectedResult));
    }
}