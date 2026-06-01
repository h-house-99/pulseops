package com.pulseops.model;

public record MonitorSummaryStats(
    Long totalChecks, 
    Long uptimeCount, 
    Double averageResponseTimeMs, 
    Long fastestResponseTimeMs, 
    Long slowestResponseTimeMs) {
}