package com.prodman.workcalendar.service;

import com.prodman.workcalendar.dto.response.DayBoardResponse;
import com.prodman.workcalendar.dto.response.ShiftSlotResponse;
import com.prodman.workcalendar.model.ShiftPattern;
import com.prodman.workcalendar.model.ShiftSlot;
import com.prodman.workcalendar.model.WorkstationDayStatus;
import com.prodman.workcalendar.model.WorkstationShift;
import com.prodman.workcalendar.repository.ShiftPatternRepository;
import com.prodman.workcalendar.repository.ShiftSlotRepository;
import com.prodman.workcalendar.repository.WorkstationDayStatusRepository;
import com.prodman.workcalendar.repository.WorkstationShiftRepository;
import com.prodman.workcalendar.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DayBoardService {

    private final WorkstationDayStatusRepository dayStatusRepository;
    private final WorkstationShiftRepository workstationShiftRepository;
    private final ShiftSlotRepository shiftSlotRepository;
    private final ShiftPatternRepository shiftPatternRepository;

    @Transactional(readOnly = true)
    public DayBoardResponse getBoard(LocalDate date) {
        UUID tenantId = TenantContext.require();

        // Статусы станков на дату (override)
        Map<UUID, WorkstationDayStatus> statusByWs = dayStatusRepository
                .findAllByTenantIdAndDate(tenantId, date).stream()
                .collect(Collectors.toMap(WorkstationDayStatus::getWorkstationId, s -> s));

        // Смены станков на дату
        List<WorkstationShift> shifts = workstationShiftRepository
                .findAllByTenantIdAndDate(tenantId, date);

        // Слоты на дату
        List<ShiftSlot> slots = shiftSlotRepository
                .findAllByTenantIdAndDate(tenantId, date);

        // Группируем слоты по workstation_id + shift_pattern_id
        Map<String, List<ShiftSlot>> slotsByWsAndPattern = slots.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getWorkstationId() + "|" + s.getShiftPatternId()));

        // Группируем смены станков по workstation_id
        Map<UUID, List<WorkstationShift>> shiftsByWs = shifts.stream()
                .collect(Collectors.groupingBy(WorkstationShift::getWorkstationId));

        // Собираем все workstation_id, для которых есть либо статус, либо смены, либо слоты
        Set<UUID> wsIds = new TreeSet<>();
        wsIds.addAll(statusByWs.keySet());
        wsIds.addAll(shiftsByWs.keySet());
        slots.forEach(s -> wsIds.add(s.getWorkstationId()));

        List<DayBoardResponse.WorkstationBoard> boards = new ArrayList<>();
        for (UUID wsId : wsIds) {
            WorkstationDayStatus status = statusByWs.get(wsId);
            Boolean isWorking = status == null ? Boolean.TRUE : status.getIsWorking();

            List<WorkstationShift> wsShifts = shiftsByWs.getOrDefault(wsId, List.of());

            List<DayBoardResponse.ShiftBoard> shiftBoards = new ArrayList<>();
            for (WorkstationShift ws : wsShifts) {
                ShiftPattern pattern = shiftPatternRepository
                        .findById(ws.getShiftPatternId()).orElse(null);
                String patternName = pattern == null ? null : pattern.getName();

                String key = wsId + "|" + ws.getShiftPatternId();
                List<ShiftSlot> cellSlots = slotsByWsAndPattern.getOrDefault(key, List.of());

                shiftBoards.add(DayBoardResponse.ShiftBoard.builder()
                        .workstationShiftId(ws.getId())
                        .shiftPatternId(ws.getShiftPatternId())
                        .shiftPatternName(patternName)
                        .totalHours(pattern == null ? null : pattern.getTotalHours())
                        .nightHours(pattern == null ? null : pattern.getNightHours())
                        .slots(cellSlots.stream().map(this::toSlotResponse).toList())
                        .build());
            }

            boards.add(DayBoardResponse.WorkstationBoard.builder()
                    .workstationId(wsId)
                    .isWorking(isWorking)
                    .shifts(shiftBoards)
                    .build());
        }

        return DayBoardResponse.builder()
                .date(date)
                .workstations(boards)
                .build();
    }

    private ShiftSlotResponse toSlotResponse(ShiftSlot s) {
        return ShiftSlotResponse.builder()
                .id(s.getId())
                .date(s.getDate())
                .workstationId(s.getWorkstationId())
                .workstationName(s.getWorkstationName())
                .shiftPatternId(s.getShiftPatternId())
                .shiftPatternName(s.getShiftPatternName())
                .requiredQualificationId(s.getRequiredQualificationId())
                .employeeId(s.getEmployeeId())
                .employeeFullName(s.getEmployeeFullName())
                .employeeDepartmentId(s.getEmployeeDepartmentId())
                .plannedHours(s.getPlannedHours())
                .plannedNightHours(s.getPlannedNightHours())
                .actualHours(s.getActualHours())
                .actualNightHours(s.getActualNightHours())
                .coefficientOverride(s.getCoefficientOverride())
                .status(s.getStatus())
                .comment(s.getComment())
                .overridden(s.getOverridden())
                .overrideReason(s.getOverrideReason())
                .isDayOffWork(s.getIsDayOffWork())
                .createdBy(s.getCreatedBy())
                .updatedBy(s.getUpdatedBy())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}