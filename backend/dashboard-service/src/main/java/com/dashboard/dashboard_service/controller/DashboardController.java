package com.dashboard.dashboard_service.controller;

import com.dashboard.dashboard_service.dto.CreateWidgetRequest;
import com.dashboard.dashboard_service.dto.DashboardResponse;
import com.dashboard.dashboard_service.dto.UpdateWidgetRequest;
import com.dashboard.dashboard_service.dto.WidgetResponse;
import com.dashboard.dashboard_service.model.Dashboard;
import com.dashboard.dashboard_service.service.DashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/dashboards")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;


    @GetMapping
    public List<DashboardResponse> getDashboards(
            @RequestAttribute("userId") UUID ownerId
    ) {
        return service.getDashboards(ownerId);
    }

    @PostMapping
    public Dashboard create(
            @RequestParam String name,
            Authentication auth
    ) {
        UUID userId = UUID.fromString(auth.getName());
        return service.createDashboard(name, userId);
    }

    @PostMapping("/{dashboardId}/widgets")
    public WidgetResponse addWidget(
            @PathVariable UUID dashboardId,
            @RequestBody @Valid CreateWidgetRequest request,
            Authentication auth
    ) {
        log.debug("Add widget request - Dashboard: {}, Auth name: {}, Principal: {}", 
                dashboardId, auth.getName(), auth.getPrincipal());
        UUID userId = UUID.fromString(auth.getName());
        return service.addWidget(dashboardId, userId, request);
    }

    @PutMapping("/{dashboardId}/widgets/{widgetId}")
    public WidgetResponse updateWidget(
            @PathVariable UUID dashboardId,
            @PathVariable UUID widgetId,
            @RequestBody @Valid UpdateWidgetRequest request,
            Authentication auth
    ) {
        log.debug("Update widget request - Dashboard: {}, Widget: {}, Auth name: {}", 
                dashboardId, widgetId, auth.getName());
        UUID userId = UUID.fromString(auth.getName());
        return service.updateWidget(dashboardId, widgetId, userId, request);
    }

    @DeleteMapping("/{dashboardId}/widgets/{widgetId}")
    public void deleteWidget(
            @PathVariable UUID dashboardId,
            @PathVariable UUID widgetId,
            Authentication auth
    ) {
        UUID userId = UUID.fromString(auth.getName());
        service.deleteWidget(dashboardId, widgetId, userId);
    }
}
