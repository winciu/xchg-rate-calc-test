package pl.rationalworks.exchangeratetest.model.dto;

import java.math.BigDecimal;
import java.util.Currency;

public record ExchangeOperationRepresentation(Currency from, Currency to, BigDecimal exchange) {
}
