package com.pulseops.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulseops.repository.CheckResultRepository;

@Service
public class CheckResultRetentionService {
    private final CheckResultRepository checkResultRepository;
    private static final Duration CHECK_RESULT_RETENTION_PERIOD = Duration.ofDays(30);

    public CheckResultRetentionService(CheckResultRepository checkResultRepository) {
        this.checkResultRepository = checkResultRepository;
    }

    @Transactional
    public void deleteExpiredCheckResults() {
        Instant retentionCutoff = Instant.now().minus(CHECK_RESULT_RETENTION_PERIOD);
        checkResultRepository.deleteByCheckedAtBefore(retentionCutoff);
    }
}
