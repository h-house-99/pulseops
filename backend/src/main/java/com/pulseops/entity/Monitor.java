package com.pulseops.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "monitors")
public class Monitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String status;

    private Integer lastStatusCode;

    private Long lastResponseTimeMs;

    private Instant lastCheckedAt;

    @Column(nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "monitor", cascade = CascadeType.REMOVE)
    private List<CheckResult> checkResults = new ArrayList<>();

    public Monitor() {

    }

    public Monitor(String name, String url, String status, Instant createdAt) {
        this.name = name;
        this.url = url;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getStatus() {
        return status;
    }

    public Integer getLastStatusCode() {
        return lastStatusCode;
    }

    public Long getLastResponseTimeMs() {
        return lastResponseTimeMs;
    }

    public Instant getLastCheckedAt() {
        return lastCheckedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void updateAfterCheck(String status, Integer statusCode, Long responseTimeMs, Instant checkedAt) {
        this.status = status;
        this.lastStatusCode = statusCode;
        this.lastResponseTimeMs = responseTimeMs;
        this.lastCheckedAt = checkedAt;
    }
}
