package com.prodman.workcalendar.service;

import com.prodman.workcalendar.dto.response.DepartmentHoursReportRow;
import com.prodman.workcalendar.dto.response.EmployeeHoursReportRow;
import com.prodman.workcalendar.repository.ShiftSlotRepository;
import com.prodman.workcalendar.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HoursReportService {

    private final ShiftSlotRepository repository;

    @Value("${employee.service.url:http://employee-service-app:8084}")
    private String employeeServiceUrl;

    private final RestClient restClient = RestClient.create();

    @Transactional(readOnly = true)
    public List<EmployeeHoursReportRow> byEmployee(LocalDate from, LocalDate to) {
        UUID tenantId = TenantContext.require();
        Map<String, Object> settings = fetchTenantSettings(tenantId);

        List<Object[]> rows = repository.aggregateHoursByEmployee(tenantId, from, to);

        return rows.stream().map(r -> {
            UUID employeeId = (UUID) r[0];
            String fullName = (String) r[1];
            UUID departmentId = (UUID) r[2];
            BigDecimal totalHours = (BigDecimal) r[3];
            BigDecimal nightHours = (BigDecimal) r[4];
            BigDecimal dayOffHours = (BigDecimal) r[5];
            long count = ((Number) r[6]).longValue();

            // смен в выходной — пересчитаем отдельно (сейчас в SQL нет count по dayOff)
            long dayOffCount = countDayOffAssignments(tenantId, employeeId, from, to);

            // нормальные часы = total − dayOff (ночные входят и в те, и в те — они "тип надбавки", не отдельная категория)
            BigDecimal normalHours = totalHours.subtract(dayOffHours);

            return EmployeeHoursReportRow.builder()
                    .employeeId(employeeId)
                    .employeeFullName(fullName)
                    .employeeDepartmentId(departmentId)
                    .totalHours(totalHours)
                    .nightHours(nightHours)
                    .dayOffHours(dayOffHours)
                    .normalHours(normalHours)
                    .assignmentsCount(count)
                    .dayOffAssignmentsCount(dayOffCount)
                    .baseCoefficientName(getString(settings, "baseCoefficientName", "Базовая смена"))
                    .baseCoefficientValue(getBigDecimal(settings, "baseCoefficientValue", BigDecimal.ONE))
                    .weekendCoefficientName(getString(settings, "weekendCoefficientName", "Работа в выходной"))
                    .weekendCoefficientValue(getBigDecimal(settings, "weekendCoefficientValue", new BigDecimal("2.00")))
                    .nightCoefficientName(getString(settings, "nightCoefficientName", "Ночная надбавка"))
                    .nightCoefficientValue(getBigDecimal(settings, "nightCoefficientValue", new BigDecimal("1.20")))
                    .build();
        }).toList();
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

    // ===== helpers =====

    /**
     * Считает количество смен сотрудника со флагом is_day_off_work = true.
     * Делаем отдельным запросом (проще, чем менять SQL).
     */
    private long countDayOffAssignments(UUID tenantId, UUID employeeId, LocalDate from, LocalDate to) {
        try {
            var slots = repository.findAllByTenantIdAndEmployeeIdAndDateBetween(
                    tenantId, employeeId, from, to);
            return slots.stream().filter(s -> Boolean.TRUE.equals(s.getIsDayOffWork())).count();
        } catch (Exception ex) {
            log.warn("Failed to count day-off for employee {}: {}", employeeId, ex.getMessage());
            return 0;
        }
    }

    /**
     * Тянет настройки тенанта из employee-service. Если недоступен — возвращает пустую map,
     * а значения возьмутся из дефолтов.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchTenantSettings(UUID tenantId) {
        try {
            return restClient.get()
                    .uri(employeeServiceUrl + "/api/tenant-settings")
                    .header("X-Tenant-Id", tenantId.toString())
                    .retrieve()
                    .body(Map.class);
        } catch (Exception ex) {
            log.warn("Failed to fetch tenant settings: {}", ex.getMessage());
            return Map.of();
        }
    }

    private String getString(Map<String, Object> m, String key, String def) {
        Object v = m.get(key);
        return v == null ? def : v.toString();
    }

    private BigDecimal getBigDecimal(Map<String, Object> m, String key, BigDecimal def) {
        Object v = m.get(key);
        if (v == null) return def;
        try {
            return new BigDecimal(v.toString());
        } catch (Exception ex) {
            return def;
        }
    }
}