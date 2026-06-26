package com.prodman.workcalendar.service;

import com.prodman.workcalendar.dto.response.EquipmentScheduleResponse;
import com.prodman.workcalendar.model.EquipmentSchedule;
import com.prodman.workcalendar.repository.EquipmentScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipmentScheduleService {

    private final EquipmentScheduleRepository equipmentScheduleRepository;

    public List<EquipmentScheduleResponse> getSchedulesByDate(LocalDate date) {
        return equipmentScheduleRepository.findByDate(date).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<EquipmentScheduleResponse> getSchedulesByDateRange(LocalDate start, LocalDate end) {
        return equipmentScheduleRepository.findByDateBetween(start, end).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public EquipmentScheduleResponse updateWorkingStatus(String id, boolean working) {
        EquipmentSchedule schedule = equipmentScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment schedule not found"));
        schedule.setWorking(working);
        return toResponse(equipmentScheduleRepository.save(schedule));
    }

    public void syncWithEquipmentMonitor() {
        // Заглушка: синхронизация с Equipment Monitor
        // В реальности здесь был бы вызов к другому МС
        System.out.println("Syncing with Equipment Monitor...");
        
        // Создаем тестовые данные, если их нет
        if (equipmentScheduleRepository.count() == 0) {
            createMockEquipmentSchedules();
        }
    }

    private void createMockEquipmentSchedules() {
        LocalDate start = LocalDate.now();
        String[] equipmentNames = {"Станок-01", "Станок-02", "Станок-03", "Станок-04", "Станок-05"};
        String[] shiftTypes = {"MORNING", "EVENING", "NIGHT", "FULL_DAY", "OFF"};

        for (int day = 0; day < 30; day++) {
            LocalDate date = start.plusDays(day);
            for (String name : equipmentNames) {
                EquipmentSchedule schedule = EquipmentSchedule.builder()
                        .equipmentId("EQ-" + name.substring(name.length() - 2))
                        .equipmentName(name)
                        .date(date)
                        .working(day % 7 != 6) // не работают по воскресеньям
                        .shiftType(shiftTypes[day % shiftTypes.length])
                        .maintenanceScheduled(day % 10 == 0)
                        .build();
                equipmentScheduleRepository.save(schedule);
            }
        }
    }

    private EquipmentScheduleResponse toResponse(EquipmentSchedule schedule) {
        return EquipmentScheduleResponse.builder()
                .id(schedule.getId())
                .equipmentId(schedule.getEquipmentId())
                .equipmentName(schedule.getEquipmentName())
                .date(schedule.getDate())
                .working(schedule.isWorking())
                .shiftType(schedule.getShiftType())
                .shiftTypeDisplay(getShiftTypeDisplay(schedule.getShiftType()))
                .note(schedule.getNote())
                .maintenanceScheduled(schedule.isMaintenanceScheduled())
                .build();
    }

    private String getShiftTypeDisplay(String shiftType) {
        return switch (shiftType) {
            case "MORNING" -> "Утро";
            case "EVENING" -> "Вечер";
            case "NIGHT" -> "Ночь";
            case "FULL_DAY" -> "Полный день";
            case "OFF" -> "Выходной";
            default -> shiftType;
        };
    }
}