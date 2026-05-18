package com.pulseops.model;

import java.time.Instant;

public record CheckResultResponse(long id, long monitorId, String status, Integer statusCode, Long responseTimeMs,
        Instant checkedAt, String errorMessage) {

}
