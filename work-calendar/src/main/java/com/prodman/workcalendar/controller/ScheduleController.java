package com.prodman.workcalendar.controller;

import com.prodman.workcalendar.dto.response.ScheduleResponse;
import com.prodman.workcalendar.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService service;

    @GetMapping
    public ResponseEntity<ScheduleResponse> get(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID departmentId) {
        return ResponseEntity.ok(service.getSchedule(from, to, departmentId));
    }
}