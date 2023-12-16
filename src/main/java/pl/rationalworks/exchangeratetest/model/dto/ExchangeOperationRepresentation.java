package pl.rationalworks.exchangeratetest.model.dto;

import java.math.BigDecimal;
import pl.rationalworks.exchangeratetest.model.Currency;

public record ExchangeOperationRepresentation(Currency from, Currency to, BigDecimal exchange) {
}
