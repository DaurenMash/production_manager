package com.prodman.workcalendar.controller;

import com.prodman.workcalendar.dto.request.CreateShiftPatternRequest;
import com.prodman.workcalendar.dto.request.UpdateShiftPatternRequest;
import com.prodman.workcalendar.dto.response.ShiftPatternResponse;
import com.prodman.workcalendar.service.ShiftPatternService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/shift-patterns")
@RequiredArgsConstructor
public class ShiftPatternController {

    private final ShiftPatternService service;

    @GetMapping
    public ResponseEntity<Page<ShiftPatternResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShiftPatternResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    public ResponseEntity<ShiftPatternResponse> create(@Valid @RequestBody CreateShiftPatternRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShiftPatternResponse> update(@PathVariable UUID id,
                                                      @Valid @RequestBody UpdateShiftPatternRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}