package com.prodman.employeeservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "employees")
public class Employee {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", updatable = false, nullable = false)
    @ToString.Include
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    /** Табельный номер. Уникален в рамках тенанта. */
    @Column(name = "code", nullable = false)
    @ToString.Include
    private String code;

    @Column(name = "first_name", nullable = false)
    @ToString.Include
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @ToString.Include
    private String lastName;

    @Column(name = "middle_name")
    private String middleName;

    /** E.164 без пробелов, например +71231231212. Логин в рамках тенанта. */
    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "hired_at")
    private LocalDate hiredAt;

    @Column(name = "fired_at")
    private LocalDate firedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private EmployeeStatus status = EmployeeStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    /** Ссылка на user-service.users.id. NULL, если у сотрудника нет учётки. */
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "max_consecutive_hours", nullable = false)
    @Builder.Default
    private Integer maxConsecutiveHours = 12;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = EmployeeStatus.AVAILABLE;
        }
        if (maxConsecutiveHours == null) {
            maxConsecutiveHours = 12;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}