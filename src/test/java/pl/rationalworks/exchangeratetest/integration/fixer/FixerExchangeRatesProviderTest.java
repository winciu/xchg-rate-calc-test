package pl.rationalworks.exchangeratetest.integration.fixer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pl.rationalworks.exchangeratetest.integration.fixer.model.FixerErrorResponse;
import pl.rationalworks.exchangeratetest.integration.fixer.model.LatestRates;
import pl.rationalworks.exchangeratetest.model.Currency;
import pl.rationalworks.exchangeratetest.properties.FixerProviderProperties;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

//@ExtendWith(SpringExtension.class)
@SpringBootTest
class FixerExchangeRatesProviderTest {

    @Autowired
    private FixerExchangeRatesProvider fixerExchangeRatesProvider;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private FixerProviderProperties properties;
    @Autowired
    private ObjectMapper objectMapper;

    private MockRestServiceServer mockServer;

    private static <T extends Map<Currency, BigDecimal>> boolean areEqual(T first, T second) {
        if (first.size() != second.size()) {
            return false;
        }

        return first.entrySet().stream()
                .allMatch(e -> e.getValue().equals(second.get(e.getKey())));
    }

    @BeforeEach
    public void init() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void shouldReturnAllLatestRates() throws IOException, FixerRatesProviderException {

        String latestUrl = UriComponentsBuilder.fromUri(properties.getLatestPath())
                .queryParam("access_key", properties.getApiKey())
                .toUriString();

        LatestRates rates = loadJsonResponse(LatestRates.class, "/integration/fixar-latest-test.json");
        mockServer.expect(ExpectedCount.once(), requestTo(latestUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writeValueAsString(rates))
                );

        LatestRates latestRates = fixerExchangeRatesProvider.provideExchangeRates();
        mockServer.verify();

        assertEquals(Currency.getInstance("EUR"), latestRates.getBase());
        assertThat(latestRates.getDate(), is(LocalDate.parse("2023-12-09")));
        assertThat(latestRates.getTimestamp(), is(Instant.ofEpochSecond(1702134123)));
        Map<Currency, BigDecimal> expectedRates = Map.of(
                Currency.getInstance("AED"), BigDecimal.valueOf(3.956462),
                Currency.getInstance("AFN"), BigDecimal.valueOf(74.649948),
                Currency.getInstance("ALL"), BigDecimal.valueOf(101.849929),
                Currency.getInstance("AMD"), BigDecimal.valueOf(434.999699),
                Currency.getInstance("ANG"), BigDecimal.valueOf(1.943299)
        );
        assertTrue(areEqual(latestRates.getRates(), expectedRates));
    }

    @Test
    void shouldReturnExceptionOnErrorResponse() throws IOException {
        String latestUrl = UriComponentsBuilder.fromUri(properties.getLatestPath())
                .queryParam("access-key", properties.getApiKey())
                .toUriString();

        FixerErrorResponse errorResponse = loadJsonResponse(FixerErrorResponse.class, "/integration/fixer-error-response.json");
        mockServer.expect(ExpectedCount.once(), requestTo(latestUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writeValueAsString(errorResponse))
                );

        FixerRatesProviderException exception = assertThrows(FixerRatesProviderException.class, () -> {
            fixerExchangeRatesProvider.provideExchangeRates();
            mockServer.verify();
        });
        assertTrue(exception.getMessage().contains("code"));
    }

    private <T> T loadJsonResponse(Class<T> typeClass, String dataFilePath) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(dataFilePath)) {
            return objectMapper.readValue(inputStream, typeClass);
        }
    }
}