package com.pulseops.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.pulseops.model.CheckResultResponse;
import com.pulseops.model.CreateMonitorRequest;
import com.pulseops.model.MonitorResponse;
import com.pulseops.service.MonitorService;

import jakarta.validation.Valid;

@RestController
public class MonitorController {
    private final MonitorService monitorService;

    public MonitorController(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @PostMapping("/api/monitors")
    public MonitorResponse createMonitor(@Valid @RequestBody CreateMonitorRequest request) {
        return monitorService.createMonitor(request);
    }

    @GetMapping("/api/monitors")
    public List<MonitorResponse> getMonitors() {
        return monitorService.getMonitors();
    }

    @PostMapping("/api/monitors/{id}/check-now")
    public CheckResultResponse checkMonitorNow(@PathVariable long id) {
        return monitorService.checkMonitorNow(id);
    }

    @GetMapping("/api/monitors/{id}/checks/recent")
    public List<CheckResultResponse> getMonitorChecks(@PathVariable long id) {
        return monitorService.getChecksForMonitor(id);
    }

    @GetMapping("/api/monitors/{id}/checks")
    public List<CheckResultResponse> getMonitorChecks(@PathVariable long id, @RequestParam(required = false, defaultValue = "24") int hours) {
        return monitorService.getChecksForMonitor(id, hours);
    }

    @DeleteMapping("/api/monitors/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMonitor(@PathVariable long id) {
        monitorService.deleteMonitor(id);
    }
}
