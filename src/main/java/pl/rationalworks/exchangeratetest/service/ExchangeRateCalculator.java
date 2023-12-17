package pl.rationalworks.exchangeratetest.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;

@Component
public class ExchangeRateCalculator {

    private static final BigDecimal BIG_DECIMAL_100 = BigDecimal.valueOf(100.000);

    public BigDecimal calculateExchangeRate(BigDecimal fromCurrencyRate, BigDecimal toCurrencyRate,
                                            BigDecimal fromCurrencySpread, BigDecimal toCurrencySpread) {
        BigDecimal rateRatio = toCurrencyRate.divide(fromCurrencyRate, MathContext.DECIMAL64);
        BigDecimal spreadBase = BIG_DECIMAL_100.subtract(toCurrencySpread.max(fromCurrencySpread));
        BigDecimal spreadRatio = spreadBase.divide(BIG_DECIMAL_100, MathContext.DECIMAL64);
        return rateRatio.multiply(spreadRatio);
    }
}
