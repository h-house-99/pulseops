package com.pulseops.service;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.pulseops.model.EndpointCheckResult;

@Component
public class RestClientEndpointCheckClient implements EndpointCheckClient {
    private final RestClient restClient = RestClient.create();

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
