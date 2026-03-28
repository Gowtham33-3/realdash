package com.dashboard.dashboard_service.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dashboards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dashboard {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private UUID ownerId;

    @OneToMany(
            mappedBy = "dashboard",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Widget> widgets = new ArrayList<>();
}