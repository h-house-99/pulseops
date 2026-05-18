package com.pulseops.service;

import com.pulseops.model.EndpointCheckResult;

public interface EndpointCheckClient {
    EndpointCheckResult checkEndpoint(String url);
}
