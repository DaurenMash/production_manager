package com.prodman.workcalendar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentReportRow {
    private UUID workstationId;
    private String workstationCode;
    private String workstationName;
    /** Сколько дней станок работал. */
    private long workingDays;
    /** Сколько смен было запланировано (workstation_shift записей). */
    private long shiftsPlanned;
    /** Сколько слотов создано. */
    private long totalSlots;
    /** Сколько слотов заполнено. */
    private long filledSlots;
    /** Сколько слотов пусто. */
    private long openSlots;
    /** Загруженность % = filledSlots / totalSlots * 100. */
    private BigDecimal utilization;
}