package com.pulseops.service;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class FailureReasonMapperTest {

    private final FailureReasonMapper failureReasonMapper = new FailureReasonMapper();

    @Test
    void mapHttpFailureToHttpStatusReason() {
        String reason = failureReasonMapper.mapFailureReason(404, null);
        assertThat(reason).isEqualTo("HTTP 404");
    }

    @Test
    void returnsNullForNullErrorMessage() {
        String reason = failureReasonMapper.mapFailureReason(200, null);
        assertThat(reason).isNull();
    }

    @Test
    void mapsTimeoutMessage() {
        assertThat(failureReasonMapper.mapFailureReason(null, "Request timeout"))
                .isEqualTo("Request timed out");
    }

    @Test
    void mapsConnectionRefusedMessage() {
        assertThat(failureReasonMapper.mapFailureReason(null, "I/O error on GET request: Connection refused"))
                .isEqualTo("Connection refused");
    }

    @Test
    void mapsExpiredCertificateMessage() {
        assertThat(failureReasonMapper.mapFailureReason(null, "(certificate_expired) PKIX path validation failed"))
                .isEqualTo("TLS certificate expired");
    }

    @Test
    void mapsRequestCancelledMessage() {
        assertThat(failureReasonMapper.mapFailureReason(null, "Request cancelled"))
                .isEqualTo("Request cancelled");
    }

    @Test
    void mapsUnknownErrorToGenericFailure() {
        assertThat(failureReasonMapper.mapFailureReason(null, "HTTP/1.1 header parser received no bytes"))
                .isEqualTo("Request failed");
    }

}
