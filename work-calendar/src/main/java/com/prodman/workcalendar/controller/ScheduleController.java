package com.prodman.workcalendar.controller;

import com.prodman.workcalendar.dto.request.CreateScheduleRequest;
import com.prodman.workcalendar.dto.request.ShiftConfigRequest;
import com.prodman.workcalendar.dto.response.ScheduleResponse;
import com.prodman.workcalendar.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedule", description = "Управление расписанием")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    @Operation(summary = "Создать расписание на день")
    public ResponseEntity<ScheduleResponse> createSchedule(@Valid @RequestBody CreateScheduleRequest request) {
        return ResponseEntity.ok(scheduleService.createSchedule(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить расписание по ID")
    public ResponseEntity<ScheduleResponse> getScheduleById(@PathVariable String id) {
        return ResponseEntity.ok(scheduleService.getScheduleById(id));
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Получить расписание на дату")
    public ResponseEntity<ScheduleResponse> getScheduleByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(scheduleService.getScheduleByDate(date));
    }

    @GetMapping("/range")
    @Operation(summary = "Получить расписание за период")
    public ResponseEntity<List<ScheduleResponse>> getSchedulesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(scheduleService.getSchedulesByDateRange(start, end));
    }

    @PutMapping("/{id}/shift-config")
    @Operation(summary = "Настроить количество смен")
    public ResponseEntity<ScheduleResponse> configureShifts(
            @PathVariable String id,
            @Valid @RequestBody ShiftConfigRequest request) {
        return ResponseEntity.ok(scheduleService.configureShifts(id, request));
    }

    @GetMapping("/conflicts")
    @Operation(summary = "Найти потенциальные конфликты")
    public ResponseEntity<List<ScheduleResponse>> findConflicts() {
        return ResponseEntity.ok(scheduleService.findConflicts());
    }
}