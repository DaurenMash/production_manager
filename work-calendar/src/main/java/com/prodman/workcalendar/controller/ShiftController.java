package com.prodman.workcalendar.controller;

import com.prodman.workcalendar.dto.response.ShiftResponse;
import com.prodman.workcalendar.service.ShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shifts")
@RequiredArgsConstructor
@Tag(name = "Shifts", description = "Управление сменами")
public class ShiftController {

    private final ShiftService shiftService;

    @PostMapping
    @Operation(summary = "Назначить смену сотруднику")
    public ResponseEntity<ShiftResponse> assignShift(
            @RequestParam String employeeId,
            @RequestParam String scheduleId,
            @RequestParam String shiftType) {
        return ResponseEntity.ok(shiftService.assignShift(employeeId, scheduleId, shiftType));
    }

    @PutMapping("/{shiftId}/status")
    @Operation(summary = "Обновить статус смены")
    public ResponseEntity<ShiftResponse> updateShiftStatus(
            @PathVariable String shiftId,
            @RequestParam boolean available) {
        return ResponseEntity.ok(shiftService.updateShiftStatus(shiftId, available));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Получить смены сотрудника")
    public ResponseEntity<List<ShiftResponse>> getShiftsByEmployee(@PathVariable String employeeId) {
        return ResponseEntity.ok(shiftService.getShiftsByEmployee(employeeId));
    }

    @GetMapping("/employee/{employeeId}/range")
    @Operation(summary = "Получить смены сотрудника за период")
    public ResponseEntity<List<ShiftResponse>> getShiftsByEmployeeAndRange(
            @PathVariable String employeeId,
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {
        return ResponseEntity.ok(shiftService.getShiftsByEmployeeAndRange(employeeId, start, end));
    }

    @GetMapping("/conflicts")
    @Operation(summary = "Найти конфликты в сменах")
    public ResponseEntity<List<ShiftResponse>> findConflicts() {
        return ResponseEntity.ok(shiftService.findConflicts());
    }
}