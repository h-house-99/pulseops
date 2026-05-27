package com.pulseops.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "check_results")
public class CheckResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monitor_id", nullable = false)
    private Monitor monitor;

    @Column(nullable = false)
    private String status;

    private Integer statusCode;
    
    private Long responseTimeMs;

    @Column(nullable = false)
    private Instant checkedAt;

    private String errorMessage;
    
    @Column(nullable = false)
    private Instant createdAt;
    
    public CheckResult() {
    }

    public CheckResult(Monitor monitor, String status, Integer statusCode, Long responseTimeMs, Instant checkedAt, String errorMessage) {
        this.monitor = monitor;
        this.status = status;
        this.statusCode = statusCode;
        this.responseTimeMs = responseTimeMs;
        this.checkedAt = checkedAt;
        this.errorMessage = errorMessage;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }
    
    public Monitor getMonitor() {
        return monitor;
    }

    public String getStatus() {
        return status;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public Instant getCheckedAt() {
        return checkedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
