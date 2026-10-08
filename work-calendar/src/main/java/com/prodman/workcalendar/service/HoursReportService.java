package com.prodman.workcalendar.service;

import com.prodman.workcalendar.dto.response.DepartmentHoursReportRow;
import com.prodman.workcalendar.dto.response.EmployeeHoursReportRow;
import com.prodman.workcalendar.repository.ShiftSlotRepository;
import com.prodman.workcalendar.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HoursReportService {

    private final ShiftSlotRepository repository;

    @Transactional(readOnly = true)
    public List<EmployeeHoursReportRow> byEmployee(LocalDate from, LocalDate to) {
        UUID tenantId = TenantContext.require();
        return repository.aggregateHoursByEmployee(tenantId, from, to).stream()
                .map(r -> EmployeeHoursReportRow.builder()
                        .employeeId((UUID) r[0])
                        .employeeFullName((String) r[1])
                        .employeeDepartmentId((UUID) r[2])
                        .totalHours((BigDecimal) r[3])
                        .nightHours((BigDecimal) r[4])
                        .dayOffHours((BigDecimal) r[5])
                        .assignmentsCount(((Number) r[6]).longValue())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DepartmentHoursReportRow> byDepartment(LocalDate from, LocalDate to) {
        UUID tenantId = TenantContext.require();
        return repository.aggregateHoursByDepartment(tenantId, from, to).stream()
                .map(r -> DepartmentHoursReportRow.builder()
                        .departmentId((UUID) r[0])
                        .totalHours((BigDecimal) r[1])
                        .nightHours((BigDecimal) r[2])
                        .assignmentsCount(((Number) r[3]).longValue())
                        .build())
                .toList();
    }
}