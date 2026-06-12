package com.pulseops.service;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.pulseops.model.EndpointCheckResult;

@Component
public class RestClientEndpointCheckClient implements EndpointCheckClient {
    private final RestClient restClient;

    public RestClientEndpointCheckClient() {

        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

        JdkClientHttpRequestFactory clientHttpRequestFactory = new JdkClientHttpRequestFactory(httpClient);
        clientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = RestClient.builder()
            .requestFactory(clientHttpRequestFactory)
            .defaultHeader("User-Agent", "PulseOps/1.0")
            .defaultHeader("Accept", "application/json")
            .build();
    }

    @Override
    public EndpointCheckResult checkEndpoint(String url) {
        try {
            return restClient.get()
                    .uri(url)
                    .exchange((request, response) -> new EndpointCheckResult(
                            response.getStatusCode().value(),
                            null));
        } catch (RestClientException exception) {
            return new EndpointCheckResult(null, exception.getMessage());
        }
    }
}
