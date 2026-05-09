package com.pulseops.service;

import com.pulseops.model.HealthResponse;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

	public HealthResponse getHealth() {
		return new HealthResponse("ok");
	}
}
