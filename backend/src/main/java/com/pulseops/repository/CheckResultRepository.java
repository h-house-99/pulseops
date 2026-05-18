package com.pulseops.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulseops.entity.CheckResult;

public interface CheckResultRepository extends JpaRepository<CheckResult, Long> {
    List<CheckResult> findByMonitorId(Long monitorId);
}
