package pl.rationalworks.exchangeratetest.controller;

import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.rationalworks.exchangeratetest.service.ExchangeRateService;

import java.math.BigDecimal;
import java.time.LocalDate;
import pl.rationalworks.exchangeratetest.model.Currency;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExchangeRateController.class)
class ExchangeRateControllerTest {

    @MockBean
    private ExchangeRateService service;

    @Autowired
    private MockMvc mvc;

    @Test
    void shouldReturnProperResponseWhenExchangeRateExists() throws Exception {
        Currency fromCurrency = Currency.getInstance("CZK");
        Currency toCurrency = Currency.getInstance("PLN");
        LocalDate date = LocalDate.now();
        BigDecimal exchangeRate = BigDecimal.valueOf(45.1234);
        BDDMockito.given(service.calculateExchangeRate(fromCurrency, toCurrency, date))
                .willReturn(Optional.of(exchangeRate));

        mvc.perform(get("/exchange")
                        .param("from", fromCurrency.getCurrencyCode())
                        .param("to", toCurrency.getCurrencyCode())
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("from", is(fromCurrency.getCurrencyCode())))
                .andExpect(jsonPath("to", is(toCurrency.getCurrencyCode())))
                .andExpect(jsonPath("exchange", equalTo(exchangeRate.doubleValue())));
    }

    @Test
    void shouldReturn404WhenExchangeRateDoesNotExist() throws Exception {
        Currency fromCurrency = Currency.getInstance("CZK");
        Currency toCurrency = Currency.getInstance("PLN");
        LocalDate date = LocalDate.now();
        BDDMockito.given(service.calculateExchangeRate(fromCurrency, toCurrency, date))
                .willReturn(Optional.empty());

        mvc.perform(get("/exchange")
                        .param("from", fromCurrency.getCurrencyCode())
                        .param("to", toCurrency.getCurrencyCode())
                        .param("date", date.toString()))
                .andExpect(status().isNotFound());
    }
}