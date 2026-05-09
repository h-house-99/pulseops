package com.pulseops.model;

public record PublicApiResponse(
                long id,
                String name,
                String description,
                String url,
                String category) {

}