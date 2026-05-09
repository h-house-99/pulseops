package com.pulseops.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pulseops.model.CreateMonitorRequest;
import com.pulseops.model.MonitorResponse;

@Service
public class MonitorService {
    private final List<MonitorResponse> monitors = new ArrayList<>();
    private int nextId = 1;

    public MonitorResponse createMonitor(CreateMonitorRequest request) {
        MonitorResponse monitor = new MonitorResponse(nextId, request.name(), request.url(), "UP", null, null, null);
        monitors.add(monitor);
        nextId++;
        return monitor;
    }

    public List<MonitorResponse> getMonitors() {
        return monitors;
    }
}
