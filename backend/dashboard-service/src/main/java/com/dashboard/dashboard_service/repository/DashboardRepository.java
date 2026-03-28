package com.dashboard.dashboard_service.repository;


import com.dashboard.dashboard_service.model.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DashboardRepository
        extends JpaRepository<Dashboard, UUID> {

    List<Dashboard> findByOwnerId(UUID ownerId);

    @Query("""
        SELECT DISTINCT d
        FROM Dashboard d
        LEFT JOIN FETCH d.widgets
        WHERE d.ownerId = :ownerId
    """)
    List<Dashboard> findAllByOwnerIdWithWidgets(UUID ownerId);

    @Query("""
        SELECT d
        FROM Dashboard d
        LEFT JOIN FETCH d.widgets
        WHERE d.id = :id
    """)
    Optional<Dashboard> findByIdWithWidgets(UUID id);
}