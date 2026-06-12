package com.pulseops.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MonitorCheckScheduler {

    private final MonitorService monitorService;
    private final CheckResultRetentionService checkResultRetentionService;
    private static final Logger logger = LoggerFactory.getLogger(MonitorCheckScheduler.class);

    public MonitorCheckScheduler(MonitorService monitorService, CheckResultRetentionService checkResultRetentionService) {
        this.monitorService = monitorService;
        this.checkResultRetentionService = checkResultRetentionService;
    }
    
    @Scheduled(cron = "${pulseops.monitor-check-cron:0 0/5 * * * *}")
    public void checkAllMonitors() {
        logger.info("Scheduled monitor checks starting");
        monitorService.checkAllMonitors();
        checkResultRetentionService.deleteExpiredCheckResults();
        logger.info("Scheduled monitor checks completed");
    }
}
