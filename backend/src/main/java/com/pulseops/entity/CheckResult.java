package com.pulseops.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "check_results")
public class CheckResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long monitorId;

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

    public CheckResult(Long monitorId, String status, Integer statusCode, Long responseTimeMs, Instant checkedAt, String errorMessage) {
        this.monitorId = monitorId;
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
    
    public Long getMonitorId() {
        return monitorId;
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
