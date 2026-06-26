package com.prodman.workcalendar.controller;

import com.prodman.workcalendar.dto.response.EquipmentScheduleResponse;
import com.prodman.workcalendar.service.EquipmentScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/equipment-schedules")
@RequiredArgsConstructor
@Tag(name = "Equipment Schedule", description = "Расписание оборудования (заглушка)")
public class EquipmentScheduleController {

    private final EquipmentScheduleService equipmentScheduleService;

    @GetMapping("/date/{date}")
    @Operation(summary = "Получить расписание оборудования на дату")
    public ResponseEntity<List<EquipmentScheduleResponse>> getSchedulesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(equipmentScheduleService.getSchedulesByDate(date));
    }

    @GetMapping("/range")
    @Operation(summary = "Получить расписание оборудования за период")
    public ResponseEntity<List<EquipmentScheduleResponse>> getSchedulesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(equipmentScheduleService.getSchedulesByDateRange(start, end));
    }

    @PutMapping("/{id}/working")
    @Operation(summary = "Обновить статус работы оборудования")
    public ResponseEntity<EquipmentScheduleResponse> updateWorkingStatus(
            @PathVariable String id,
            @RequestParam boolean working) {
        return ResponseEntity.ok(equipmentScheduleService.updateWorkingStatus(id, working));
    }

    @PostMapping("/sync")
    @Operation(summary = "Синхронизировать с Equipment Monitor (заглушка)")
    public ResponseEntity<Void> syncWithEquipmentMonitor() {
        equipmentScheduleService.syncWithEquipmentMonitor();
        return ResponseEntity.ok().build();
    }
}