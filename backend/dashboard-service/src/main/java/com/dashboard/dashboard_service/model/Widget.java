package com.dashboard.dashboard_service.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "widgets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Widget {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode config;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dashboard_id", nullable = false)
    private Dashboard dashboard;
}
