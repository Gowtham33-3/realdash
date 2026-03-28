package com.dashboard.dashboard_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateWidgetRequest(
        @NotBlank String type,
        @NotNull Object config
) {}