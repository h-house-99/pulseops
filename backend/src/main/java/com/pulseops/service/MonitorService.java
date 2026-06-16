package com.pulseops.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.pulseops.config.ReadOnlyModeConfig;
import com.pulseops.entity.CheckResult;
import com.pulseops.entity.Monitor;
import com.pulseops.model.CheckResultResponse;
import com.pulseops.model.CreateMonitorRequest;
import com.pulseops.model.EndpointCheckResult;
import com.pulseops.model.MonitorResponse;
import com.pulseops.model.MonitorSummaryStats;
import com.pulseops.repository.CheckResultRepository;
import com.pulseops.repository.MonitorRepository;

@Service
public class MonitorService {
    
    private final EndpointCheckClient endpointCheckClient;
    private final MonitorRepository monitorRepository;
    private final CheckResultRepository checkResultRepository;
    private final FailureReasonMapper failureReasonMapper;
    private static final Logger logger = LoggerFactory.getLogger(MonitorService.class);
    private final ReadOnlyModeConfig readOnlyModeConfig;

    public MonitorService(EndpointCheckClient endpointCheckClient, MonitorRepository monitorRepository, CheckResultRepository checkResultRepository, FailureReasonMapper failureReasonMapper, ReadOnlyModeConfig readOnlyModeConfig) {
        this.endpointCheckClient = endpointCheckClient;
        this.monitorRepository = monitorRepository;
        this.checkResultRepository = checkResultRepository;
        this.failureReasonMapper = failureReasonMapper;
        this.readOnlyModeConfig = readOnlyModeConfig;
    }

    public MonitorResponse createMonitor(CreateMonitorRequest request) {
        ensureMonitorManagementAllowed();
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
        ensureMonitorManagementAllowed();
        Monitor monitor = monitorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monitor not found"));

        CheckResult checkResult = checkAndSaveResult(monitor);
        return toCheckResultResponse(checkResult, monitor.getId());
    }

    public void checkAllMonitors() {
        List<Monitor> monitors = monitorRepository.findAllByOrderByIdAsc();
        for (Monitor monitor : monitors) {
            try {
                checkAndSaveResult(monitor);
            } catch (Exception e) {
                logger.warn("Scheduled check failed for monitor {}", monitor.getId(), e);
            }
        }
    }

    public List<CheckResultResponse> getChecksForMonitor(long monitorId) {
        Monitor monitor = monitorRepository.findById(monitorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monitor not found"));

        return checkResultRepository.findTop5ByMonitorOrderByCheckedAtDesc(monitor)
                .stream()
                .map(checkResult -> toCheckResultResponse(checkResult, monitorId))
                .toList();
    }

    public List<CheckResultResponse> getChecksForMonitor(long monitorId, int hours) {
        if (hours != 1 && hours != 8 && hours != 24 && hours != 168) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported hours value");
        }

        Monitor monitor = monitorRepository.findById(monitorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monitor not found"));

        return checkResultRepository.findByMonitorAndCheckedAtGreaterThanEqualOrderByCheckedAtAsc(monitor, Instant.now().minus(Duration.ofHours(hours)))
                .stream()
                .map(checkResult -> toCheckResultResponse(checkResult, monitorId))
                .toList();
    }

    public void deleteMonitor(long monitorId) {
        ensureMonitorManagementAllowed();
        Monitor monitor = monitorRepository.findById(monitorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monitor not found"));
        monitorRepository.delete(monitor);
    }

    private MonitorResponse toResponse(Monitor monitor) {
        MonitorSummaryStats summaryStats = checkResultRepository.getSummaryStatsByMonitor(monitor);

        Long uptimePercentage = null;
        if (summaryStats.totalChecks() > 0) {
            uptimePercentage = Math.round(((double) summaryStats.uptimeCount() * 100) / summaryStats.totalChecks());
        }
        Long averageResponseTimeMs = summaryStats.averageResponseTimeMs() != null ? Math.round(summaryStats.averageResponseTimeMs()) : null;

        String lastNonNullErrorMessage = checkResultRepository.findTopByMonitorAndErrorMessageIsNotNullOrderByCheckedAtDesc(monitor)
                .map(CheckResult::getErrorMessage)
                .orElse(null);

        Optional<CheckResult> latestFailureCheck = checkResultRepository.findTopByMonitorAndStatusOrderByCheckedAtDesc(monitor, "DOWN");

        String lastFailureReason = latestFailureCheck
                .map(checkResult -> failureReasonMapper.mapFailureReason(checkResult.getStatusCode(), checkResult.getErrorMessage()))
                .orElse(null);

        Instant lastFailureAt = latestFailureCheck
                .map(checkResult -> checkResult.getCheckedAt())
                .orElse(null);


        return new MonitorResponse(monitor.getId(), monitor.getName(), monitor.getUrl(), monitor.getStatus(), monitor.getLastStatusCode(), monitor.getLastResponseTimeMs(), monitor.getLastCheckedAt(), summaryStats.totalChecks(), uptimePercentage, averageResponseTimeMs, summaryStats.fastestResponseTimeMs(), summaryStats.slowestResponseTimeMs(), lastNonNullErrorMessage, lastFailureReason, lastFailureAt);
    }

    private CheckResultResponse toCheckResultResponse(CheckResult checkResult, long monitorId) {
        return new CheckResultResponse(checkResult.getId(), monitorId, checkResult.getStatus(), checkResult.getStatusCode(), checkResult.getResponseTimeMs(), checkResult.getCheckedAt(), checkResult.getErrorMessage());
    }

    private CheckResult checkAndSaveResult(Monitor monitor) {
        Instant startedAt = Instant.now();
        EndpointCheckResult endpointCheckResult = endpointCheckClient.checkEndpoint(monitor.getUrl());
        Instant checkedAt = Instant.now();
        Long responseTimeMs = Duration.between(startedAt, checkedAt).toMillis();
        Integer statusCode = endpointCheckResult.statusCode();
        String errorMessage = endpointCheckResult.errorMessage();
        String status = statusCode != null && statusCode >= 200 && statusCode < 400 ? "UP" : "DOWN";

        monitor.updateAfterCheck(status, statusCode, responseTimeMs, checkedAt);
        monitorRepository.save(monitor);
        CheckResult checkResult = new CheckResult(monitor, status, statusCode, responseTimeMs, checkedAt, errorMessage);
        checkResultRepository.save(checkResult);
        return checkResult;
    }

    private void ensureMonitorManagementAllowed() {
        if (readOnlyModeConfig.isReadOnlyMode()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Monitor management is not allowed in read-only mode");
        }
    }
}
