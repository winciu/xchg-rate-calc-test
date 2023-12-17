package pl.rationalworks.exchangeratetest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;

import java.util.List;

import static java.util.Objects.nonNull;

/**
 * We use our class for storing currencies since the {@link java.util.Currency} does not support {@code BTC} as the
 * currency symbol. <br/>
 * So this class is kind of a wrapper/extension for the default {@link java.util.Currency} class provided by the JDK.<br/>
 * It's better to use a proper objects than having a 'primitive' (String) obsession.
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Currency implements Comparable<Currency> {
    /**
     * List of currency codes that are not supported in native {@link java.util.Currency} class.
     */
    private static final List<String> UNSUPPORTED_CURRENCY_CODES = List.of("BTC", "GGP", "IMP", "JEP");
    @EqualsAndHashCode.Include
    @JsonValue
    private final String currencyCode;

    private final java.util.Currency currency;

    private Currency(String currencyCode) {
        this.currencyCode = currencyCode;
        if (!UNSUPPORTED_CURRENCY_CODES.contains(currencyCode)) {
            this.currency = java.util.Currency.getInstance(currencyCode);
        } else {
            this.currency = null;
        }
    }

    @JsonCreator
    public static Currency getInstance(String currencyCode) {
        return new Currency(currencyCode);
    }

    public String getCurrencyCode() {
        if (nonNull(currency)) {
            return currency.getCurrencyCode();
        }
        return currencyCode;
    }

    @Override
    public String toString() {
        return currencyCode;
    }

    @Override
    public int compareTo(Currency otherCurrency) {
        return currencyCode.compareTo(otherCurrency.currencyCode);
    }
}
