package com.prodman.workcalendar.service;

import com.prodman.workcalendar.dto.response.EquipmentReportRow;
import com.prodman.workcalendar.model.ShiftSlot;
import com.prodman.workcalendar.model.ShiftSlotStatus;
import com.prodman.workcalendar.model.WorkstationDayStatus;
import com.prodman.workcalendar.repository.ShiftSlotRepository;
import com.prodman.workcalendar.repository.WorkstationDayStatusRepository;
import com.prodman.workcalendar.repository.WorkstationShiftRepository;
import com.prodman.workcalendar.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipmentReportService {

    private final WorkstationDayStatusRepository dayStatusRepository;
    private final WorkstationShiftRepository workstationShiftRepository;
    private final ShiftSlotRepository shiftSlotRepository;

    @Transactional(readOnly = true)
    public List<EquipmentReportRow> report(LocalDate from, LocalDate to) {
        UUID tenantId = TenantContext.require();

        // Слоты
        List<ShiftSlot> slots = shiftSlotRepository
                .findAllByTenantIdAndDateBetween(tenantId, from, to);

        // Смены (для подсчёта рабочих дней)
        var wsShifts = workstationShiftRepository
                .findAllByTenantIdAndDateBetween(tenantId, from, to);

        // Статусы станков
        List<WorkstationDayStatus> statuses = dayStatusRepository
                .findAllByTenantIdAndDateBetween(tenantId, from, to);

        // Группируем слоты по workstation_id
        Map<UUID, List<ShiftSlot>> slotsByWs = slots.stream()
                .collect(Collectors.groupingBy(ShiftSlot::getWorkstationId));

        // Группируем смены по workstation_id
        Map<UUID, Set<LocalDate>> daysByWs = new HashMap<>();
        for (var w : wsShifts) {
            daysByWs.computeIfAbsent(w.getWorkstationId(), k -> new HashSet<>()).add(w.getDate());
        }

        // Считаем по каждому станку
        Set<UUID> allWsIds = new HashSet<>();
        allWsIds.addAll(slotsByWs.keySet());
        allWsIds.addAll(daysByWs.keySet());

        List<EquipmentReportRow> rows = new ArrayList<>();
        for (UUID wsId : allWsIds) {
            List<ShiftSlot> wsSlots = slotsByWs.getOrDefault(wsId, List.of());
            Set<LocalDate> workingDays = daysByWs.getOrDefault(wsId, Set.of());

            long totalSlots = wsSlots.stream()
                    .filter(s -> s.getStatus() != ShiftSlotStatus.CANCELLED)
                    .count();
            long filledSlots = wsSlots.stream()
                    .filter(s -> s.getStatus() == ShiftSlotStatus.FILLED)
                    .count();
            long openSlots = Math.max(0, totalSlots - filledSlots);
            long shiftsPlanned = wsShifts.stream()
                    .filter(w -> w.getWorkstationId().equals(wsId))
                    .count();

            BigDecimal utilization = totalSlots == 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(filledSlots)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(totalSlots), 2, RoundingMode.HALF_UP);

            String code = null;
            String name = null;
            ShiftSlot any = wsSlots.isEmpty() ? null : wsSlots.get(0);
            if (any != null) {
                name = any.getWorkstationName();
            }

            rows.add(EquipmentReportRow.builder()
                    .workstationId(wsId)
                    .workstationCode(code)
                    .workstationName(name)
                    .workingDays(workingDays.size())
                    .shiftsPlanned(shiftsPlanned)
                    .totalSlots(totalSlots)
                    .filledSlots(filledSlots)
                    .openSlots(openSlots)
                    .utilization(utilization)
                    .build());
        }

        // Сортировка по имени станка
        rows.sort(Comparator.comparing(r -> r.getWorkstationName() == null ? "" : r.getWorkstationName()));

        // Приглушаем warning про неиспользуемый параметр
        void_unused(statuses);
        return rows;
    }

    // Заглушка, чтобы не было warning от IDE про неиспользуемые statuses
    private void void_unused(Object x) { /* no-op */ }
}