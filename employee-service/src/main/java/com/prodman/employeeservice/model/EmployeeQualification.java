package com.prodman.employeeservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Связь сотрудник ↔ квалификация.
 * Хранит уровень владения, дату присвоения и кто присвоил.
 * Целостность "employee и qualification из одного тенанта" — на уровне сервиса.
 */
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "employee_qualifications")
public class EmployeeQualification {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "qualification_id", nullable = false)
    private Qualification qualification;

    /** Уровень владения (>= 1). 1 — начинающий. */
    @Column(name = "level", nullable = false)
    @Builder.Default
    private Integer level = 1;

    @Column(name = "assigned_at", nullable = false)
    @Builder.Default
    private LocalDate assignedAt = LocalDate.now();

    /** user_id того, кто присвоил. NULL для системных. */
    @Column(name = "assigned_by")
    private UUID assignedBy;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (level == null) {
            level = 1;
        }
        if (assignedAt == null) {
            assignedAt = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}