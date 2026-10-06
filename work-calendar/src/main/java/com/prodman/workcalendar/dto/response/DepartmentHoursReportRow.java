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
public class DepartmentHoursReportRow {
    private UUID departmentId;
    private BigDecimal totalHours;
    private BigDecimal nightHours;
    private long assignmentsCount;
}