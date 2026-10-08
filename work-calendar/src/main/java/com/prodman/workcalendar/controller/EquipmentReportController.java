package com.prodman.workcalendar.controller;

import com.prodman.workcalendar.dto.response.EquipmentReportRow;
import com.prodman.workcalendar.service.EquipmentReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports/equipment")
@RequiredArgsConstructor
public class EquipmentReportController {

    private final EquipmentReportService service;

    @GetMapping
    public ResponseEntity<List<EquipmentReportRow>> report(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(service.report(from, to));
    }
}