package com.prodman.workcalendar.controller;

import com.prodman.workcalendar.dto.request.AddWorkstationShiftRequest;
import com.prodman.workcalendar.dto.response.WorkstationShiftResponse;
import com.prodman.workcalendar.service.WorkstationShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workstation-shifts")
@RequiredArgsConstructor
public class WorkstationShiftController {

    private final WorkstationShiftService service;

    @GetMapping
    public ResponseEntity<List<WorkstationShiftResponse>> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(service.listByDate(date));
    }

    @PostMapping
    public ResponseEntity<WorkstationShiftResponse> add(@Valid @RequestBody AddWorkstationShiftRequest req) {
        return ResponseEntity.ok(service.add(req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}