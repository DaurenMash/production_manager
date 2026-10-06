package com.prodman.workcalendar.controller;

import com.prodman.workcalendar.dto.request.SetWorkstationDayStatusRequest;
import com.prodman.workcalendar.dto.response.WorkstationDayStatusResponse;
import com.prodman.workcalendar.service.WorkstationDayStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workstation-day-status")
@RequiredArgsConstructor
public class WorkstationDayStatusController {

    private final WorkstationDayStatusService service;

    @GetMapping
    public ResponseEntity<List<WorkstationDayStatusResponse>> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(service.listByDate(date));
    }

    @PostMapping
    public ResponseEntity<WorkstationDayStatusResponse> set(@Valid @RequestBody SetWorkstationDayStatusRequest req) {
        return ResponseEntity.ok(service.setStatus(req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}