package com.pulseops.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pulseops.model.PublicApiResponse;
import com.pulseops.service.PublicApiService;

@RestController
public class PublicApiController {
    private final PublicApiService publicApiService;

    public PublicApiController(PublicApiService publicApiService) {
        this.publicApiService = publicApiService;
    }

    @GetMapping("/api/public-apis")
    public List<PublicApiResponse> getPublicApis() {
        return publicApiService.getPublicApis();
    }

}
