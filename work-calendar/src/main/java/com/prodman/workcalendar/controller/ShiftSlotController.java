package com.prodman.workcalendar.controller;

import com.prodman.workcalendar.dto.request.AssignEmployeeToSlotRequest;
import com.prodman.workcalendar.dto.request.CreateShiftSlotRequest;
import com.prodman.workcalendar.dto.request.UpdateShiftSlotRequest;
import com.prodman.workcalendar.dto.response.ShiftSlotResponse;
import com.prodman.workcalendar.service.ShiftSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shift-slots")
@RequiredArgsConstructor
public class ShiftSlotController {

    private final ShiftSlotService service;

    @GetMapping
    public ResponseEntity<List<ShiftSlotResponse>> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(service.listByDate(date));
    }

    @GetMapping("/by-workstation")
    public ResponseEntity<List<ShiftSlotResponse>> listByWorkstation(
            @RequestParam UUID workstationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(service.listByWorkstationAndDate(workstationId, date));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShiftSlotResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    public ResponseEntity<ShiftSlotResponse> create(@Valid @RequestBody CreateShiftSlotRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShiftSlotResponse> update(@PathVariable UUID id,
                                                    @Valid @RequestBody UpdateShiftSlotRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<ShiftSlotResponse> assign(@PathVariable UUID id,
                                                    @Valid @RequestBody AssignEmployeeToSlotRequest req) {
        return ResponseEntity.ok(service.assignEmployee(id, req));
    }

    @PostMapping("/{id}/unassign")
    public ResponseEntity<ShiftSlotResponse> unassign(@PathVariable UUID id) {
        return ResponseEntity.ok(service.unassignEmployee(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}