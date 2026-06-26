package com.prodman.workcalendar.service;

import com.prodman.workcalendar.dto.request.CreateScheduleRequest;
import com.prodman.workcalendar.dto.request.ShiftConfigRequest;
import com.prodman.workcalendar.dto.response.ScheduleResponse;
import com.prodman.workcalendar.dto.response.ShiftResponse;
import com.prodman.workcalendar.model.Schedule;
import com.prodman.workcalendar.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ShiftService shiftService;

    public ScheduleResponse createSchedule(CreateScheduleRequest request) {
        if (scheduleRepository.findByDate(request.getDate()).isPresent()) {
            throw new RuntimeException("Schedule already exists for this date");
        }

        Schedule schedule = Schedule.builder()
                .date(request.getDate())
                .shiftConfig(request.getShiftConfig() != null ? request.getShiftConfig() : "3_SHIFTS_8H")
                .notes(request.getNotes())
                .build();

        Schedule saved = scheduleRepository.save(schedule);

        // Автоматически создаем смены
        shiftService.createDefaultShiftsForSchedule(saved);

        return toResponse(saved);
    }

    public ScheduleResponse getScheduleById(String id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        return toResponse(schedule);
    }

    public ScheduleResponse getScheduleByDate(LocalDate date) {
        Schedule schedule = scheduleRepository.findByDate(date)
                .orElseThrow(() -> new RuntimeException("Schedule not found for date: " + date));
        return toResponse(schedule);
    }

    public List<ScheduleResponse> getSchedulesByDateRange(LocalDate start, LocalDate end) {
        return scheduleRepository.findByDateBetween(start, end).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ScheduleResponse configureShifts(String id, ShiftConfigRequest request) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        schedule.setShiftConfig(request.getShiftConfig());
        schedule.setNotes(request.getNotes());

        // Пересоздаем смены с новой конфигурацией
        shiftService.deleteShiftsForSchedule(schedule.getId());
        shiftService.createDefaultShiftsForSchedule(schedule);

        return toResponse(scheduleRepository.save(schedule));
    }

    public List<ScheduleResponse> findConflicts() {
        LocalDate now = LocalDate.now();
        List<Schedule> schedules = scheduleRepository.findByDateBetween(now, now.plusDays(30));
        
        List<ScheduleResponse> responses = new ArrayList<>();
        for (Schedule schedule : schedules) {
            ScheduleResponse response = toResponse(schedule);
            List<String> conflicts = shiftService.findConflictsForDate(schedule.getDate());
            if (!conflicts.isEmpty()) {
                response.setHasConflicts(true);
                response.setConflicts(conflicts);
            }
            responses.add(response);
        }
        return responses;
    }

    private ScheduleResponse toResponse(Schedule schedule) {
        List<ShiftResponse> shiftResponses = shiftService.getShiftsByScheduleId(schedule.getId());

        return ScheduleResponse.builder()
                .id(schedule.getId())
                .date(schedule.getDate())
                .shiftConfig(schedule.getShiftConfig())
                .shiftConfigDisplay(schedule.getShiftConfig().equals("2_SHIFTS_12H") ? "2 смены (12ч)" : "3 смены (8ч)")
                .shifts(shiftResponses)
                .notes(schedule.getNotes())
                .hasConflicts(false)
                .conflicts(new ArrayList<>())
                .build();
    }
}