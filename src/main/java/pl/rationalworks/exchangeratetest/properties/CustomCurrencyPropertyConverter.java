package pl.rationalworks.exchangeratetest.properties;

import org.springframework.core.convert.converter.Converter;
import pl.rationalworks.exchangeratetest.model.Currency;

/**
 * Since we are using custom Currency class due to an issue for undefined/unsupported codes (see javadoc in {@link Currency} class),
 * we need a converter to properly read values (codes) set in the configuration property files.<br/><br/>
 * Instead of having this converter we there are two other methods for handling conversion properly as described at
 * {@linkplain https://reflectoring.io/spring-boot-configuration-properties/}.<br/>
 * This is:
 * <ul><li>the {@link Currency} class provides a constructor that takes a single String as an argument</li>
 * <li>the {@link Currency} class provides a static valueOf method that takes a single String as an argument and returns a {@link Currency} object.</li></ul>
 * <br/><br/>
 * I have not decided to use those alternatives since I wanted the custom currency class {@link Currency} to be as much similar as possible to the
 * original {@link java.util.Currency} class. Adding additional methods like {@code valueOf} or a custom public constructor could
 * lead to additional problems later with potential class replacement. My intention was, that doing simple Find/Replace all {@code import}
 * statements + removing helper code (like this class) should be enough.
 */
public class CustomCurrencyPropertyConverter implements Converter<String, Currency> {
    @Override
    public Currency convert(String source) {
        return Currency.getInstance(source);
    }
}
