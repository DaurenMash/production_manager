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

    /** Все оплачиваемые часы за период (включая ночные и выходные). */
    private BigDecimal totalHours;

    /** Из них ночные. */
    private BigDecimal nightHours;

    /** Из них часы в выходной. */
    private BigDecimal dayOffHours;

    /** Обычные (total - night - dayOff + пересечения; см. расчёт в сервисе). */
    private BigDecimal normalHours;

    /** Сколько смен всего. */
    private long assignmentsCount;

    /** Сколько смен в выходной. */
    private long dayOffAssignmentsCount;

    // === Коэффициенты (из tenant_settings, одинаковы на весь тенант) ===
    private String baseCoefficientName;
    private BigDecimal baseCoefficientValue;
    private String weekendCoefficientName;
    private BigDecimal weekendCoefficientValue;
    private String nightCoefficientName;
    private BigDecimal nightCoefficientValue;
}