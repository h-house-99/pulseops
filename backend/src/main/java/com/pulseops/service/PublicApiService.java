package com.pulseops.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pulseops.model.PublicApiResponse;

@Service
public class PublicApiService {
    public List<PublicApiResponse> getPublicApis() {
        return List.of(
                new PublicApiResponse(1, "GitHub API", "Public GitHub REST API", "https://api.github.com",
                        "Developer Tools"),
                new PublicApiResponse(2, "JSONPlaceholder", "Fake REST API for testing and prototyping",
                        "https://jsonplaceholder.typicode.com/posts", "Testing"),
                new PublicApiResponse(3, "HTTPBin", "HTTP request and response testing service",
                        "https://httpbin.org/status/200", "Testing"));
    }
}
