package com.pulseops.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    private final PulseOpsConfig pulseOpsConfig;

    public CorsConfig(PulseOpsConfig pulseOpsConfig) {
        this.pulseOpsConfig = pulseOpsConfig;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(pulseOpsConfig.getCorsAllowedOrigins())
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*");
    }
}
