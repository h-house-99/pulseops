package com.pulseops.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulseops.entity.CheckResult;
import com.pulseops.entity.Monitor;

public interface CheckResultRepository extends JpaRepository<CheckResult, Long> {
    List<CheckResult> findTop5ByMonitorOrderByCheckedAtDesc(Monitor monitor);
}
