package com.prodman.employeeservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Настройки трудовых правил тенанта.
 * Одна запись на тенант (tenant_id = PK).
 * Создаётся лениво при первом обращении (если записи нет — создаём с дефолтами).
 */
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "tenant_settings")
public class TenantSettings {

    /** Совпадает с Tenant.id. Одна запись = один тенант. */
    @Id
    @Column(name = "tenant_id", updatable = false, nullable = false)
    private UUID tenantId;

    /**
     * Что делать со сменами, переходящими через полночь.
     * KEEP_START_DAY — все часы в день начала.
     * SPLIT — разделить по календарным дням.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "month_crossing_mode", nullable = false)
    @Builder.Default
    private MonthCrossingMode monthCrossingMode = MonthCrossingMode.KEEP_START_DAY;

    @Column(name = "base_coefficient_name", nullable = false)
    @Builder.Default
    private String baseCoefficientName = "Базовая смена";

    @Column(name = "base_coefficient_value", nullable = false)
    @Builder.Default
    private BigDecimal baseCoefficientValue = BigDecimal.ONE;

    @Column(name = "weekend_coefficient_name", nullable = false)
    @Builder.Default
    private String weekendCoefficientName = "Работа в выходной";

    @Column(name = "weekend_coefficient_value", nullable = false)
    @Builder.Default
    private BigDecimal weekendCoefficientValue = new BigDecimal("2.00");

    @Column(name = "night_coefficient_name", nullable = false)
    @Builder.Default
    private String nightCoefficientName = "Ночная надбавка";

    @Column(name = "night_coefficient_value", nullable = false)
    @Builder.Default
    private BigDecimal nightCoefficientValue = new BigDecimal("1.20");

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (monthCrossingMode == null) monthCrossingMode = MonthCrossingMode.KEEP_START_DAY;
        if (baseCoefficientValue == null) baseCoefficientValue = BigDecimal.ONE;
        if (weekendCoefficientValue == null) weekendCoefficientValue = new BigDecimal("2.00");
        if (nightCoefficientValue == null) nightCoefficientValue = new BigDecimal("1.20");
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}