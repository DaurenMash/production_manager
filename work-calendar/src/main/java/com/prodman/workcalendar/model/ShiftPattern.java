package com.prodman.workcalendar.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "shift_patterns")
public class ShiftPattern {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", updatable = false, nullable = false)
    @ToString.Include
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "code", nullable = false)
    @ToString.Include
    private String code;

    @Column(name = "name", nullable = false)
    @ToString.Include
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "crosses_midnight", nullable = false)
    @Builder.Default
    private Boolean crossesMidnight = false;

    @Column(name = "total_hours", nullable = false)
    private BigDecimal totalHours;

    @Column(name = "night_hours", nullable = false)
    @Builder.Default
    private BigDecimal nightHours = BigDecimal.ZERO;

    @Column(name = "night_window_start")
    private LocalTime nightWindowStart;

    @Column(name = "night_window_end")
    private LocalTime nightWindowEnd;

    @Column(name = "coefficient", nullable = false)
    @Builder.Default
    private BigDecimal coefficient = BigDecimal.ONE;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

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
        if (crossesMidnight == null) crossesMidnight = false;
        if (nightHours == null) nightHours = BigDecimal.ZERO;
        if (coefficient == null) coefficient = BigDecimal.ONE;
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}