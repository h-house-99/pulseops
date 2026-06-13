package com.pulseops.service;

import java.io.EOFException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.nio.channels.UnresolvedAddressException;
import java.security.cert.CertificateException;
import java.time.Duration;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.pulseops.model.EndpointCheckResult;

@Component
public class RestClientEndpointCheckClient implements EndpointCheckClient {
    private final RestClient restClient;
    private static final Logger logger = LoggerFactory.getLogger(RestClientEndpointCheckClient.class);

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
            Throwable rootCause = getRootCause(exception);

            if (rootCause instanceof UnknownHostException || rootCause instanceof UnresolvedAddressException) {
                return new EndpointCheckResult(null, "DNS resolution failed");
            }

            if (rootCause instanceof ConnectException) {
                return new EndpointCheckResult(null, "Connection refused");
            }

            if (rootCause instanceof SocketTimeoutException) {
                return new EndpointCheckResult(null, "Request timed out");
            }

            if (rootCause instanceof SSLException || rootCause instanceof CertificateException) {
                return new EndpointCheckResult(null, "TLS certificate expired");
            }

            if (rootCause instanceof EOFException) {
                return new EndpointCheckResult(null, "Connection closed unexpectedly");
            }

            String exceptionMessage = exception.getMessage() == null ? "" : exception.getMessage().toLowerCase();

            if (exceptionMessage.contains("request cancelled") || exceptionMessage.contains("request canceled")) {
                return new EndpointCheckResult(null, "Request cancelled");
            }

            String rootCauseType = rootCause == null ? "" : rootCause.getClass().getName();
            String rootCauseMessage = rootCause == null ? "" : rootCause.getMessage();

            logger.warn(
                    "Unmapped endpoint check failure for url: {}, exceptionType: {}, exceptionMessage: {}, rootCauseType: {}, rootCauseMessage: {}",
                    url,
                    exception.getClass().getName(),
                    exceptionMessage,
                    rootCauseType,
                    rootCauseMessage);

            return new EndpointCheckResult(null, "Request failed");
        }
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable rootCause = throwable;

        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }

        return rootCause;
    }
}
