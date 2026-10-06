package com.prodman.workcalendar.controller;

import com.prodman.workcalendar.dto.response.DepartmentHoursReportRow;
import com.prodman.workcalendar.dto.response.EmployeeHoursReportRow;
import com.prodman.workcalendar.service.HoursReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports/hours")
@RequiredArgsConstructor
public class HoursReportController {

    private final HoursReportService service;

    @GetMapping("/by-employee")
    public ResponseEntity<List<EmployeeHoursReportRow>> byEmployee(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(service.byEmployee(from, to));
    }

    @GetMapping("/by-department")
    public ResponseEntity<List<DepartmentHoursReportRow>> byDepartment(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(service.byDepartment(from, to));
    }
}