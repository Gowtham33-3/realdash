package com.dashboard.dashboard_service.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

public record WidgetResponse(
        UUID id,
        String type,
        JsonNode config,
        UUID dashboardId
) {}