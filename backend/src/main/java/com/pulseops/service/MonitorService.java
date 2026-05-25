package com.pulseops.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.pulseops.entity.CheckResult;
import com.pulseops.entity.Monitor;
import com.pulseops.model.CheckResultResponse;
import com.pulseops.model.CreateMonitorRequest;
import com.pulseops.model.EndpointCheckResult;
import com.pulseops.model.MonitorResponse;
import com.pulseops.repository.CheckResultRepository;
import com.pulseops.repository.MonitorRepository;

@Service
public class MonitorService {
    
    private final EndpointCheckClient endpointCheckClient;
    private final MonitorRepository monitorRepository;
    private final CheckResultRepository checkResultRepository;

    public MonitorService(EndpointCheckClient endpointCheckClient, MonitorRepository monitorRepository, CheckResultRepository checkResultRepository) {
        this.endpointCheckClient = endpointCheckClient;
        this.monitorRepository = monitorRepository;
        this.checkResultRepository = checkResultRepository;
    }

    public MonitorResponse createMonitor(CreateMonitorRequest request) {
        Monitor monitor = new Monitor(request.name(), request.url(), "UNKNOWN", Instant.now());
        monitorRepository.save(monitor);
        return toResponse(monitor);
    }

    public List<MonitorResponse> getMonitors() {
        return monitorRepository.findAllByOrderByIdAsc()
                .stream()
                .map(monitor -> toResponse(monitor))
                .toList();
    }

    public CheckResultResponse checkMonitorNow(long id) {
        Monitor monitor = monitorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monitor not found"));

        Instant startedAt = Instant.now();
        EndpointCheckResult endpointCheckResult = endpointCheckClient.checkEndpoint(monitor.getUrl());
        Instant checkedAt = Instant.now();
        Integer statusCode = endpointCheckResult.statusCode();
        String errorMessage = endpointCheckResult.errorMessage();
        Long responseTimeMs = Duration.between(startedAt, checkedAt).toMillis();
        String status = statusCode != null && statusCode >= 200 && statusCode < 400 ? "UP" : "DOWN";

        monitor.updateAfterCheck(status, statusCode, responseTimeMs, checkedAt);
        monitorRepository.save(monitor);
        CheckResult checkResult = new CheckResult(monitor.getId(), status, statusCode, responseTimeMs, checkedAt, errorMessage);
        checkResultRepository.save(checkResult);
        return toCheckResultResponse(checkResult);
    }

    public List<CheckResultResponse> getChecksForMonitor(long monitorId) {
        Monitor monitor = monitorRepository.findById(monitorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monitor not found"));

        return checkResultRepository.findTop5ByMonitorIdOrderByCheckedAtDesc(monitor.getId())
                .stream()
                .map(checkResult -> toCheckResultResponse(checkResult))
                .toList();
    }

    private MonitorResponse toResponse(Monitor monitor) {
        return new MonitorResponse(monitor.getId(), monitor.getName(), monitor.getUrl(), monitor.getStatus(), monitor.getLastStatusCode(), monitor.getLastResponseTimeMs(), monitor.getLastCheckedAt());
    }

    private CheckResultResponse toCheckResultResponse(CheckResult checkResult) {
        return new CheckResultResponse(checkResult.getId(), checkResult.getMonitorId(), checkResult.getStatus(), checkResult.getStatusCode(), checkResult.getResponseTimeMs(), checkResult.getCheckedAt(), checkResult.getErrorMessage());
    }
}
