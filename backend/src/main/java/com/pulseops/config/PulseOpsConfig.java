package com.pulseops.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PulseOpsConfig {
    @Value("${pulseops.read-only-mode}")
    private boolean readOnlyMode;

    @Value("${pulseops.seed-curated-monitors}")
    private boolean seedCuratedMonitors;

    @Value("${pulseops.cors-allowed-origins}")
    private String corsAllowedOrigins;

    public boolean isReadOnlyMode() {
        return readOnlyMode;
    }

    public boolean isSeedCuratedMonitors() {
        return seedCuratedMonitors;
    }

    public String getCorsAllowedOrigins() {
        return corsAllowedOrigins;
    }
}
