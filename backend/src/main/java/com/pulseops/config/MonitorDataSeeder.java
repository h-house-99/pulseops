package com.pulseops.config;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.pulseops.config.CuratedMonitorDefinition.CuratedMonitor;
import com.pulseops.entity.Monitor;
import com.pulseops.repository.MonitorRepository;

@Component
public class MonitorDataSeeder implements ApplicationRunner {
    private final MonitorRepository monitorRepository;
    private final PulseOpsConfig pulseOpsConfig;
    private final Logger logger = LoggerFactory.getLogger(MonitorDataSeeder.class);

    public MonitorDataSeeder(MonitorRepository monitorRepository, PulseOpsConfig pulseOpsConfig) {
        this.monitorRepository = monitorRepository;
        this.pulseOpsConfig = pulseOpsConfig;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!pulseOpsConfig.isSeedCuratedMonitors()) {
            return;
        }

        if (monitorRepository.count() > 0) {
            logger.info("Skipping seeding of curated monitors because monitors already exist");
            return;
        }

        for (CuratedMonitor curatedMonitor : CuratedMonitorDefinition.all()) {
            Monitor monitor = new Monitor(
                    curatedMonitor.name(),
                    curatedMonitor.url(),
                    "UNKNOWN",
                    Instant.now());
            monitorRepository.save(monitor);
        }

        logger.info("Seeded {} curated monitors", CuratedMonitorDefinition.all().size());
    }

}
