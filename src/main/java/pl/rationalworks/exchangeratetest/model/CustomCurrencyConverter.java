package pl.rationalworks.exchangeratetest.model;

import jakarta.persistence.AttributeConverter;

public class CustomCurrencyConverter implements AttributeConverter<Currency, String> {
    @Override
    public String convertToDatabaseColumn(Currency currency) {
        return currency.getCurrencyCode();
    }

    @Override
    public Currency convertToEntityAttribute(String dbValue) {
        return Currency.getInstance(dbValue);
    }
}
