package com.prodman.workcalendar.controller;

import com.prodman.workcalendar.dto.response.DayBoardResponse;
import com.prodman.workcalendar.service.DayBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/day-board")
@RequiredArgsConstructor
public class DayBoardController {

    private final DayBoardService service;

    @GetMapping
    public ResponseEntity<DayBoardResponse> get(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(service.getBoard(date));
    }
}