package com.pulseops.service;

import org.springframework.stereotype.Service;

import com.pulseops.model.HealthResponse;

@Service
public class HealthService {

	public HealthResponse getHealth() {
		return new HealthResponse("ok");
	}
}
