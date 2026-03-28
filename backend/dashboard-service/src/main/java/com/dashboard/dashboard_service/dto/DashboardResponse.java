package com.dashboard.dashboard_service.dto;

import java.util.List;
import java.util.UUID;

public record DashboardResponse(
        UUID id,
        String name,
        UUID ownerId,
        List<WidgetResponse> widgets
) {}