package com.pulseops.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateMonitorRequest(@NotBlank String name, @NotBlank @Pattern(regexp = "^https?://.*$") String url) {

}
