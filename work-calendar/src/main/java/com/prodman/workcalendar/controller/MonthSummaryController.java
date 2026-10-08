package com.prodman.workcalendar.controller;

import com.prodman.workcalendar.dto.response.MonthSummaryResponse;
import com.prodman.workcalendar.service.MonthSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/day-board/month-summary")
@RequiredArgsConstructor
public class MonthSummaryController {

    private final MonthSummaryService service;

    @GetMapping
    public ResponseEntity<MonthSummaryResponse> get(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(service.getSummary(from, to));
    }
}