package com.pulseops.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pulseops.entity.CheckResult;
import com.pulseops.entity.Monitor;
import com.pulseops.model.MonitorSummaryStats;

public interface CheckResultRepository extends JpaRepository<CheckResult, Long> {
    List<CheckResult> findTop5ByMonitorOrderByCheckedAtDesc(Monitor monitor);

    List<CheckResult> findByMonitorAndCheckedAtGreaterThanEqualOrderByCheckedAtAsc(Monitor monitor, Instant checkedAt);

    @Query("""
            SELECT new com.pulseops.model.MonitorSummaryStats(
                COUNT(cr),
                COALESCE(SUM(CASE WHEN cr.status = 'UP' THEN 1 ELSE 0 END), 0),
                AVG(cr.responseTimeMs),
                MIN(cr.responseTimeMs),
                MAX(cr.responseTimeMs)
            )
            FROM CheckResult cr
            WHERE cr.monitor = :monitor
            """)
    MonitorSummaryStats getSummaryStatsByMonitor(Monitor monitor);

    Optional<CheckResult> findTopByMonitorAndErrorMessageIsNotNullOrderByCheckedAtDesc(Monitor monitor);

    Optional<CheckResult> findTopByMonitorAndStatusOrderByCheckedAtDesc(Monitor monitor, String status);

    void deleteByCheckedAtBefore(Instant checkedAt);
}
