package com.pulseops.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MonitorCheckScheduler {

    private final MonitorService monitorService;

    public MonitorCheckScheduler(MonitorService monitorService) {
        this.monitorService = monitorService;
    }
    
    @Scheduled(cron = "0 0/5 * * * *")
    public void checkAllMonitors() {
        monitorService.checkAllMonitors();
    }
}