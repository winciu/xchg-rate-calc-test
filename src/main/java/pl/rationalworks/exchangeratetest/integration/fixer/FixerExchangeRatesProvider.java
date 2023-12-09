package pl.rationalworks.exchangeratetest.integration.fixer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pl.rationalworks.exchangeratetest.properties.FixerProviderProperties;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class FixerExchangeRatesProvider {

    private final RestTemplate restTemplate;
    private final FixerProviderProperties properties;

    public LatestRates provideExchangeRates() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        String latestUrl = UriComponentsBuilder.fromUri(properties.getLatestPath())
                .queryParam("access-key", properties.getApiKey())
                .toUriString();

        ResponseEntity<LatestRates> response = restTemplate.exchange(latestUrl, HttpMethod.GET, entity, LatestRates.class);
        return response.getBody();
    }
}
