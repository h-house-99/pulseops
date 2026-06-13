package com.pulseops.service;

import org.springframework.stereotype.Component;
@Component
public class FailureReasonMapper {

    public String mapFailureReason(Integer statusCode, String errorMessage) {
        if (statusCode != null && (statusCode < 200 || statusCode >= 400)) {
            return "HTTP " + statusCode;
        }

        if (errorMessage == null || errorMessage.isBlank()) {
            return null;
        }

        String normalized = errorMessage.toLowerCase();


        if (normalized.contains("timeout") || normalized.contains("timed out")) {
            return "Request timed out";
        }

        if (normalized.contains("connection refused")) {
            return "Connection refused";
        }

        if (normalized.contains("dns resolution failed")) {
            return "DNS resolution failed";
        }

        if (normalized.contains("tls certificate expired")) {
            return "TLS certificate expired";
        }

        if (normalized.contains("request cancelled") || normalized.contains("request canceled")) {
            return "Request cancelled";
        }

        if (normalized.contains("connection closed unexpectedly")) {
            return "Connection closed unexpectedly";
        }

        return "Request failed";
    }
}
