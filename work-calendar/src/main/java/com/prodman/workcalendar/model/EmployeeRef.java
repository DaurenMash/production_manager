package com.prodman.workcalendar.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "employees_ref")
public class EmployeeRef {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /** Статус из employee-service: AVAILABLE / BUSY / VACATION / SICK_LEAVE / UNAVAILABLE / FIRED. */
    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    @PrePersist
    @PreUpdate
    protected void touch() {
        syncedAt = LocalDateTime.now();
    }
}