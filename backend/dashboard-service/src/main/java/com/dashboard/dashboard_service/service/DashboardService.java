package com.dashboard.dashboard_service.service;

import com.dashboard.dashboard_service.dto.CreateWidgetRequest;
import com.dashboard.dashboard_service.dto.DashboardResponse;
import com.dashboard.dashboard_service.dto.UpdateWidgetRequest;
import com.dashboard.dashboard_service.dto.WidgetResponse;
import com.dashboard.dashboard_service.model.Dashboard;
import com.dashboard.dashboard_service.model.Widget;
import com.dashboard.dashboard_service.repository.DashboardRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardRepository repository;
    private final DashboardEventPublisher publisher;
    private final ObjectMapper objectMapper;

    @Transactional
    public Dashboard createDashboard(String name, UUID userId) {
        Dashboard dashboard = Dashboard.builder()
                .id(UUID.randomUUID())
                .name(name)
                .ownerId(userId)
                .build();
        return repository.save(dashboard);
    }

    @Transactional
    public List<DashboardResponse> getDashboards(UUID ownerId) {
        return repository.findAllByOwnerIdWithWidgets(ownerId).stream()
                .map(d -> new DashboardResponse(
                        d.getId(),
                        d.getName(),
                        d.getOwnerId(),
                        d.getWidgets().stream()
                                .map(w -> new WidgetResponse(w.getId(), w.getType(), w.getConfig(), d.getId()))
                                .toList()
                ))
                .toList();
    }

    @Transactional
    public WidgetResponse addWidget(UUID dashboardId, UUID userId, CreateWidgetRequest request) {
        log.debug("Adding widget to dashboard {} for user {}", dashboardId, userId);
        Dashboard dashboard = getOwnedDashboard(dashboardId, userId);

        Widget widget = Widget.builder()
                .id(UUID.randomUUID())
                .type(request.type())
                .config(objectMapper.valueToTree(request.config()))
                .dashboard(dashboard)
                .build();

        dashboard.getWidgets().add(widget);
        repository.save(dashboard);

        publisher.publish("WIDGET_ADDED", dashboardId, Map.of(
                "widgetId", widget.getId(),
                "type", widget.getType(),
                "config", request.config()
        ));

        return toResponse(widget);
    }

    @Transactional
    public WidgetResponse updateWidget(UUID dashboardId, UUID widgetId, UUID userId, UpdateWidgetRequest request) {
        Dashboard dashboard = getOwnedDashboard(dashboardId, userId);

        Widget widget = dashboard.getWidgets().stream()
                .filter(w -> w.getId().equals(widgetId))
                .findFirst()
                .orElseThrow();

        widget.setConfig(objectMapper.valueToTree(request.config()));
        repository.save(dashboard);

        publisher.publish("WIDGET_UPDATED", dashboardId, Map.of(
                "widgetId", widgetId,
                "config", request.config()
        ));

        return toResponse(widget);
    }

    @Transactional
    public void deleteWidget(UUID dashboardId, UUID widgetId, UUID userId) {
        Dashboard dashboard = getOwnedDashboard(dashboardId, userId);
        dashboard.getWidgets().removeIf(w -> w.getId().equals(widgetId));
        repository.save(dashboard);

        publisher.publish("WIDGET_REMOVED", dashboardId, Map.of("widgetId", widgetId));
    }

    private WidgetResponse toResponse(Widget widget) {
        return new WidgetResponse(
                widget.getId(),
                widget.getType(),
                widget.getConfig(),
                widget.getDashboard().getId()
        );
    }

    private Dashboard getOwnedDashboard(UUID dashboardId, UUID userId) {
        Dashboard dashboard = repository.findById(dashboardId).orElseThrow();

        log.debug("Dashboard owner: {}, Requesting user: {}", dashboard.getOwnerId(), userId);

        if (!dashboard.getOwnerId().equals(userId)) {
            log.warn("Access denied: User {} attempted to access dashboard {} owned by {}",
                    userId, dashboardId, dashboard.getOwnerId());
            throw new AccessDeniedException("Forbidden");
        }
        return dashboard;
    }
}
