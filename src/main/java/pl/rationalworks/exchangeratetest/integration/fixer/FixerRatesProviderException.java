package pl.rationalworks.exchangeratetest.integration.fixer;

public class FixerRatesProviderException extends Exception{
    public FixerRatesProviderException(String message) {
        super(message);
    }

    public FixerRatesProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
