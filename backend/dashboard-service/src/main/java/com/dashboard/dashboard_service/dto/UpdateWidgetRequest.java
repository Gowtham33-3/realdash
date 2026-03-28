package com.dashboard.dashboard_service.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateWidgetRequest(
        @NotNull Object config
) {}
