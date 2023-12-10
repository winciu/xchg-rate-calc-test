package pl.rationalworks.exchangeratetest.integration.fixer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pl.rationalworks.exchangeratetest.integration.fixer.model.FixerResponse;
import pl.rationalworks.exchangeratetest.integration.fixer.model.FixerErrorResponse;
import pl.rationalworks.exchangeratetest.integration.fixer.model.LatestRates;
import pl.rationalworks.exchangeratetest.properties.FixerProviderProperties;

import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@Component
@Slf4j
@RequiredArgsConstructor
public class FixerExchangeRatesProvider {

    private final RestTemplate restTemplate;
    private final FixerProviderProperties properties;

    public LatestRates provideExchangeRates() throws FixerRatesProviderException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        String latestUrl = UriComponentsBuilder.fromUri(properties.getLatestPath())
                .queryParam("access-key", properties.getApiKey())
                .toUriString();

        ResponseEntity<FixerResponse> response = restTemplate.exchange(latestUrl, HttpMethod.GET, entity, FixerResponse.class);
        FixerResponse bodyResponse = response.getBody();
        if (!requireNonNull(bodyResponse).isSuccess()) {
            FixerErrorResponse errorResponse = (FixerErrorResponse) bodyResponse;
            String msg =  format("Error getting latest rates. %s", errorResponse.getError());
            log.error(msg, errorResponse.getError());
            throw new FixerRatesProviderException(msg);
        }
        return (LatestRates) bodyResponse;
    }
}
