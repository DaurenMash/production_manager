package com.prodman.workcalendar.dto.response;

import com.prodman.workcalendar.model.ShiftSlotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftSlotResponse {
    private UUID id;
    private LocalDate date;
    private UUID workstationId;
    private String workstationName;
    private UUID shiftPatternId;
    private String shiftPatternName;
    private UUID requiredQualificationId;
    private UUID employeeId;
    private String employeeFullName;
    private UUID employeeDepartmentId;
    private BigDecimal plannedHours;
    private BigDecimal plannedNightHours;
    private BigDecimal actualHours;
    private BigDecimal actualNightHours;
    private BigDecimal coefficientOverride;
    private ShiftSlotStatus status;
    private String comment;
    private Boolean overridden;
    private String overrideReason;
    private Boolean isDayOffWork;
    private UUID createdBy;
    private UUID updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}