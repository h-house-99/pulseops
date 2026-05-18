package com.pulseops.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulseops.entity.Monitor;

public interface MonitorRepository extends JpaRepository<Monitor, Long> {
    
}
