package com.pulseops.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReadOnlyModeConfig {
    @Value("${pulseops.read-only-mode}")
    private boolean readOnlyMode;

    public boolean isReadOnlyMode() {
        return readOnlyMode;
    }
}
