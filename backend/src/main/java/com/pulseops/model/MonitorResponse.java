package com.pulseops.model;

import java.time.Instant;

public record MonitorResponse(
        long id,
        String name,
        String url,
        String status,
        Integer lastStatusCode,
        Long lastResponseTimeMs,
        Instant lastCheckedAt,
        Long totalChecks,
        Long uptimePercentage,
        Long averageResponseTimeMs,
        Long fastestResponseTimeMs,
        Long slowestResponseTimeMs,
        String latestErrorMessage,
        String latestFailureReason,
        Instant lastFailureAt) {

}
