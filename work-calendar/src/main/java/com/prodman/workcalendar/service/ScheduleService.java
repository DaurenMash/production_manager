package com.prodman.workcalendar.service;

import com.prodman.workcalendar.dto.response.ScheduleResponse;
import com.prodman.workcalendar.model.EmployeeRef;
import com.prodman.workcalendar.model.ShiftPattern;
import com.prodman.workcalendar.model.ShiftSlot;
import com.prodman.workcalendar.repository.EmployeeRefRepository;
import com.prodman.workcalendar.repository.ShiftPatternRepository;
import com.prodman.workcalendar.repository.ShiftSlotRepository;
import com.prodman.workcalendar.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ShiftSlotRepository shiftSlotRepository;
    private final ShiftPatternRepository shiftPatternRepository;
    private final EmployeeRefRepository employeeRefRepository;

    @Transactional(readOnly = true)
    public ScheduleResponse getSchedule(LocalDate from, LocalDate to, UUID departmentId) {
        UUID tenantId = TenantContext.require();

        List<EmployeeRef> employees = employeeRefRepository.findAllByTenantId(tenantId);
        if (departmentId != null) {
            employees = employees.stream()
                    .filter(e -> departmentId.equals(e.getDepartmentId()))
                    .toList();
        }

        // Слоты за период, только FILLED
        List<ShiftSlot> slots = shiftSlotRepository
                .findAllByTenantIdAndDateBetween(tenantId, from, to)
                .stream()
                .filter(s -> s.getEmployeeId() != null)
                .filter(s -> s.getStatus() != null
                        && s.getStatus().name().equals("FILLED"))
                .toList();

        // Группируем слоты по (employeeId, date)
        Map<String, List<ShiftSlot>> byEmpDate = slots.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getEmployeeId() + "|" + s.getDate()));

        // Паттерны — заранее, чтобы не дёргать на каждый слот
        Map<UUID, ShiftPattern> patternsById = shiftPatternRepository.findAll().stream()
                .collect(Collectors.toMap(ShiftPattern::getId, p -> p));

        List<LocalDate> days = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) days.add(d);

        List<ScheduleResponse.Row> rows = employees.stream()
                .sorted(Comparator
                        .comparing((EmployeeRef e) -> e.getDepartmentId() == null ? "" : e.getDepartmentId().toString())
                        .thenComparing(e -> e.getFullName() == null ? "" : e.getFullName()))
                .map(emp -> {

                    List<ScheduleResponse.Cell> cells = new ArrayList<>();
                    for (LocalDate d : days) {
                        cells.add(buildCell(emp, d, byEmpDate.get(emp.getId() + "|" + d), patternsById));
                    }

                    return ScheduleResponse.Row.builder()
                            .employeeId(emp.getId())
                            .employeeFullName(emp.getFullName())
                            .departmentId(emp.getDepartmentId())
                            .employeeStatus(emp.getStatus())
                            .days(cells)
                            .build();
                })
                .toList();

        return ScheduleResponse.builder()
                .from(from)
                .to(to)
                .employees(rows)
                .build();
    }

    private ScheduleResponse.Cell buildCell(
            EmployeeRef emp,
            LocalDate date,
            List<ShiftSlot> slotsOnDay,
            Map<UUID, ShiftPattern> patternsById) {

        // 1. Статус «отпуск / больничный / уволен / недоступен» — приоритет
        String empStatus = emp.getStatus();
        if ("VACATION".equals(empStatus)) {
            return ScheduleResponse.Cell.builder().date(date).kind("VACATION").build();
        }
        if ("SICK_LEAVE".equals(empStatus)) {
            return ScheduleResponse.Cell.builder().date(date).kind("SICK_LEAVE").build();
        }
        if ("FIRED".equals(empStatus)) {
            return ScheduleResponse.Cell.builder().date(date).kind("FIRED").build();
        }
        if ("UNAVAILABLE".equals(empStatus)) {
            return ScheduleResponse.Cell.builder().date(date).kind("UNAVAILABLE").build();
        }

        // 2. Есть смена — отдаём первую (обычно одна на день)
        if (slotsOnDay != null && !slotsOnDay.isEmpty()) {
            ShiftSlot s = slotsOnDay.get(0);
            ShiftPattern p = s.getShiftPatternId() == null
                    ? null : patternsById.get(s.getShiftPatternId());

            return ScheduleResponse.Cell.builder()
                    .date(date)
                    .kind("SHIFT")
                    .shiftCode(p != null ? p.getCode() : null)
                    .shiftName(p != null ? p.getName() : s.getShiftPatternName())
                    .color(p != null ? p.getColor() : null)
                    .isDayOffWork(s.getIsDayOffWork())
                    .totalHours(s.getPlannedHours())
                    .nightHours(s.getPlannedNightHours())
                    .workstationId(s.getWorkstationId())
                    .workstationName(s.getWorkstationName())
                    .build();
        }

        // 3. Смены нет — выходной (AVAILABLE / BUSY)
        return ScheduleResponse.Cell.builder()
                .date(date)
                .kind("DAY_OFF")
                .build();
    }
}