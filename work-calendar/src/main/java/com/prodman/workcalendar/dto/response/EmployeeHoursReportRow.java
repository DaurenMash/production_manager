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
public class EmployeeHoursReportRow {
    private UUID employeeId;
    private String employeeFullName;
    private UUID employeeDepartmentId;
    private BigDecimal totalHours;
    private BigDecimal nightHours;
    /** Часы, отработанные в выходной (по флагу is_day_off_work). */
    private BigDecimal dayOffHours;
    private long assignmentsCount;
}