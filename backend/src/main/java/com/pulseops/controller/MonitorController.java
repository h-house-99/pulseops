package com.pulseops.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.pulseops.model.CheckResultResponse;
import com.pulseops.model.CreateMonitorRequest;
import com.pulseops.model.MonitorResponse;
import com.pulseops.service.MonitorService;

@RestController
public class MonitorController {
    private final MonitorService monitorService;

    public MonitorController(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @PostMapping("/api/monitors")
    public MonitorResponse createMonitor(@RequestBody CreateMonitorRequest request) {
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

    @GetMapping("/api/monitors/{id}/checks")
    public List<CheckResultResponse> getMonitorChecks(@PathVariable long id) {
        return monitorService.getChecksForMonitor(id);
    }
}
