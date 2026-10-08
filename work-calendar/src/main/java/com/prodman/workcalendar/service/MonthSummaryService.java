package com.prodman.workcalendar.service;

import com.prodman.workcalendar.dto.response.MonthSummaryResponse;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonthSummaryService {

    private final WorkstationDayStatusRepository dayStatusRepository;
    private final WorkstationShiftRepository workstationShiftRepository;
    private final ShiftSlotRepository shiftSlotRepository;

    @Transactional(readOnly = true)
    public MonthSummaryResponse getSummary(LocalDate from, LocalDate to) {
        UUID tenantId = TenantContext.require();

        // Статусы станков (override) за период
        List<WorkstationDayStatus> statuses = dayStatusRepository
                .findAllByTenantIdAndDateBetween(tenantId, from, to);
        Map<String, WorkstationDayStatus> statusByKey = statuses.stream()
                .collect(Collectors.toMap(
                        s -> s.getWorkstationId() + "|" + s.getDate(),
                        s -> s));

        // Смены станков за период
        var wsShifts = workstationShiftRepository.findAllByTenantIdAndDateBetween(tenantId, from, to);

        // Слоты за период
        List<ShiftSlot> slots = shiftSlotRepository.findAllByTenantIdAndDateBetween(tenantId, from, to);

        // Группируем слоты по дате + workstation_id
        Map<String, List<ShiftSlot>> slotsByDateWs = slots.stream()
                .collect(Collectors.groupingBy(s -> s.getDate() + "|" + s.getWorkstationId()));

        // Группируем смены по дате (для «работающих» станков)
        Map<LocalDate, Set<UUID>> wsByDate = new HashMap<>();
        for (var w : wsShifts) {
            wsByDate.computeIfAbsent(w.getDate(), k -> new HashSet<>()).add(w.getWorkstationId());
        }

        // Итерируем по дням
        List<MonthSummaryResponse.DaySummary> days = new ArrayList<>();
        LocalDate d = from;
        while (!d.isAfter(to)) {
            final LocalDate dd = d;

            // Собираем множество станков, которые «работали» в этот день:
            // 1) те, что явно помечены is_working (или не помечены, но есть в ws_shifts);
            // 2) без явного override — считаются работающими, если есть смены.
            Set<UUID> workingIds = new HashSet<>();
            Set<UUID> involvedIds = new HashSet<>();

            // из смен станков
            Set<UUID> wsIds = wsByDate.getOrDefault(dd, Set.of());
            involvedIds.addAll(wsIds);
            for (UUID wsId : wsIds) {
                WorkstationDayStatus st = statusByKey.get(wsId + "|" + dd);
                if (st == null || Boolean.TRUE.equals(st.getIsWorking())) {
                    workingIds.add(wsId);
                }
            }

            // из слотов (на случай, если есть слоты, но смена не отмечена)
            for (ShiftSlot s : slots) {
                if (s.getDate().equals(dd)) {
                    involvedIds.add(s.getWorkstationId());
                    if (!workingIds.contains(s.getWorkstationId())) {
                        WorkstationDayStatus st = statusByKey.get(s.getWorkstationId() + "|" + dd);
                        if (st == null || Boolean.TRUE.equals(st.getIsWorking())) {
                            workingIds.add(s.getWorkstationId());
                        }
                    }
                }
            }

            int totalSlots = 0;
            int filledSlots = 0;
            for (UUID wsId : involvedIds) {
                List<ShiftSlot> cell = slotsByDateWs.getOrDefault(dd + "|" + wsId, List.of());
                for (ShiftSlot s : cell) {
                    if (s.getStatus() == ShiftSlotStatus.CANCELLED) continue;
                    totalSlots++;
                    if (s.getStatus() == ShiftSlotStatus.FILLED) filledSlots++;
                }
            }

            // Список работающих станков (id + имя станка, если есть слоты)
            List<MonthSummaryResponse.DaySummary.WorkstationRef> wsRefs = workingIds.stream()
                    .map(id -> {
                        ShiftSlot anySlot = slots.stream()
                                .filter(s -> s.getDate().equals(dd) && s.getWorkstationId().equals(id))
                                .findFirst().orElse(null);
                        return MonthSummaryResponse.DaySummary.WorkstationRef.builder()
                                .id(id)
                                .name(anySlot == null ? null : anySlot.getWorkstationName())
                                .build();
                    })
                    .collect(Collectors.toList());

            days.add(MonthSummaryResponse.DaySummary.builder()
                    .date(dd)
                    .totalWorkstations(involvedIds.size())
                    .workingWorkstations(workingIds.size())
                    .totalSlots(totalSlots)
                    .filledSlots(filledSlots)
                    .openSlots(Math.max(0, totalSlots - filledSlots))
                    .workstations(wsRefs)
                    .build());

            d = d.plusDays(1);
        }

        return MonthSummaryResponse.builder()
                .from(from)
                .to(to)
                .days(days)
                .build();
    }
}