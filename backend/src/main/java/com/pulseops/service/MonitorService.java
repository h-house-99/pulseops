package com.pulseops.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.pulseops.model.CheckResultResponse;
import com.pulseops.model.CreateMonitorRequest;
import com.pulseops.model.EndpointCheckResult;
import com.pulseops.model.MonitorResponse;

@Service
public class MonitorService {
    private final List<MonitorResponse> monitors = new ArrayList<>();
    private final List<CheckResultResponse> checkResults = new ArrayList<>();
    private final EndpointCheckClient endpointCheckClient;
    private int nextId = 1;
    private long nextCheckId = 1;

    public MonitorService(EndpointCheckClient endpointCheckClient) {
        this.endpointCheckClient = endpointCheckClient;
    }

    public MonitorResponse createMonitor(CreateMonitorRequest request) {
        MonitorResponse monitor = new MonitorResponse(nextId, request.name(), request.url(), "UNKNOWN", null, null,
                null);
        monitors.add(monitor);
        nextId++;
        return monitor;
    }

    public List<MonitorResponse> getMonitors() {
        return monitors;
    }

    public CheckResultResponse checkMonitorNow(long id) {
        MonitorResponse monitor = findMonitor(id);

        Instant startedAt = Instant.now();
        EndpointCheckResult endpointCheckResult = endpointCheckClient.checkEndpoint(monitor.url());
        Integer statusCode = endpointCheckResult.statusCode();
        String errorMessage = endpointCheckResult.errorMessage();
        Instant checkedAt = Instant.now();
        Long responseTimeMs = Duration.between(startedAt, checkedAt).toMillis();
        String status = statusCode != null && statusCode >= 200 && statusCode < 400 ? "UP" : "DOWN";

        updateMonitor(monitor, status, statusCode, responseTimeMs, checkedAt);
        CheckResultResponse checkResult = new CheckResultResponse(
                nextCheckId,
                monitor.id(),
                status,
                statusCode,
                responseTimeMs,
                checkedAt,
                errorMessage);
        nextCheckId++;
        checkResults.add(checkResult);
        return checkResult;
    }

    public List<CheckResultResponse> getChecksForMonitor(long monitorId) {
        findMonitor(monitorId);

        return checkResults.stream()
                .filter(checkResult -> checkResult.monitorId() == monitorId)
                .toList();
    }

    private MonitorResponse findMonitor(long id) {
        return monitors.stream()
                .filter(m -> m.id() == id)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monitor not found"));
    }

    private void updateMonitor(MonitorResponse monitor, String status, Integer statusCode, Long responseTimeMs,
            Instant checkedAt) {
        MonitorResponse updatedMonitor = new MonitorResponse(monitor.id(), monitor.name(), monitor.url(), status,
                statusCode, responseTimeMs, checkedAt);
        monitors.set(monitors.indexOf(monitor), updatedMonitor);
    }
}
