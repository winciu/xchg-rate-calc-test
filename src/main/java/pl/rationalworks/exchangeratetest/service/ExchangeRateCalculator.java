package pl.rationalworks.exchangeratetest.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ExchangeRateCalculator {


    private static final BigDecimal BIG_DECIMAL_100 = BigDecimal.valueOf(100.00);

    public BigDecimal calculateExchangeRate(BigDecimal toCurrencyRate,
                                            BigDecimal fromCurrencyRate,
                                            BigDecimal toCurrencySpread,
                                            BigDecimal fromCurrencySpread) {
        BigDecimal rateRatio = toCurrencyRate.divide(fromCurrencyRate);
        BigDecimal spreadBase = BIG_DECIMAL_100.subtract(toCurrencySpread.max(fromCurrencySpread));
        BigDecimal spreadRatio = spreadBase.divide(BIG_DECIMAL_100);
        return rateRatio.multiply(spreadRatio);
    }
}
