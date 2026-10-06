package com.prodman.workcalendar.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
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
@Table(name = "shift_slots")
public class ShiftSlot {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "workstation_id", nullable = false)
    private UUID workstationId;

    @Column(name = "workstation_name")
    private String workstationName;

    @Column(name = "shift_pattern_id", nullable = false)
    private UUID shiftPatternId;

    @Column(name = "shift_pattern_name")
    private String shiftPatternName;

    @Column(name = "required_qualification_id")
    private UUID requiredQualificationId;

    @Column(name = "employee_id")
    private UUID employeeId;

    @Column(name = "employee_full_name")
    private String employeeFullName;

    @Column(name = "employee_department_id")
    private UUID employeeDepartmentId;

    @Column(name = "planned_hours", nullable = false)
    private BigDecimal plannedHours;

    @Column(name = "planned_night_hours", nullable = false)
    @Builder.Default
    private BigDecimal plannedNightHours = BigDecimal.ZERO;

    @Column(name = "actual_hours")
    private BigDecimal actualHours;

    @Column(name = "actual_night_hours")
    private BigDecimal actualNightHours;

    @Column(name = "coefficient_override")
    private BigDecimal coefficientOverride;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ShiftSlotStatus status = ShiftSlotStatus.OPEN;

    @Column(name = "comment")
    private String comment;

    @Column(name = "overridden", nullable = false)
    @Builder.Default
    private Boolean overridden = false;

    @Column(name = "override_reason")
    private String overrideReason;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (plannedNightHours == null) plannedNightHours = BigDecimal.ZERO;
        if (overridden == null) overridden = false;
        if (status == null) status = ShiftSlotStatus.OPEN;
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}