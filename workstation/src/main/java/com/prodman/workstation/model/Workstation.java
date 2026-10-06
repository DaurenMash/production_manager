package com.prodman.workstation.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "workstations")
public class Workstation {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", updatable = false, nullable = false)
    @ToString.Include
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    /** Отдел, к которому привязана станция. Ссылка на employee-service.departments (логическая). */
    @Column(name = "department_id", nullable = false)
    private UUID departmentId;

    /** Требуемая квалификация для работы на станции. Ссылка на employee-service.qualifications (логическая). Может быть NULL. */
    @Column(name = "required_qualification_id")
    private UUID requiredQualificationId;

    @Column(name = "code", nullable = false)
    @ToString.Include
    private String code;

    @Column(name = "name", nullable = false)
    @ToString.Include
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /** user_id из user-service, кто создал. */
    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (isActive == null) isActive = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}