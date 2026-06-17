package com.pulseops.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.pulseops.config.CuratedMonitorDefinition;
import com.pulseops.config.CuratedMonitorDefinition.CuratedMonitor;
import com.pulseops.model.PublicApiResponse;

@Service
public class PublicApiService {
    public List<PublicApiResponse> getPublicApis() {
        List<CuratedMonitor> curatedMonitors = CuratedMonitorDefinition.all();
        List<PublicApiResponse> publicApiResponses = new ArrayList<>();
        AtomicInteger index = new AtomicInteger(1);
        for (CuratedMonitor curatedMonitor : curatedMonitors) {
            publicApiResponses.add(new PublicApiResponse(index.getAndIncrement(), curatedMonitor.name(), curatedMonitor.url()));
        }
        return publicApiResponses;
    }
}
