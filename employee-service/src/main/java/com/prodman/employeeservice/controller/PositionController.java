package com.prodman.employeeservice.controller;

import com.prodman.employeeservice.dto.request.CreatePositionRequest;
import com.prodman.employeeservice.dto.request.UpdatePositionRequest;
import com.prodman.employeeservice.dto.response.PositionResponse;
import com.prodman.employeeservice.service.PositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @GetMapping
    public ResponseEntity<Page<PositionResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(positionService.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PositionResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(positionService.get(id));
    }

    @PostMapping
    public ResponseEntity<PositionResponse> create(@Valid @RequestBody CreatePositionRequest req) {
        return ResponseEntity.ok(positionService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PositionResponse> update(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdatePositionRequest req) {
        return ResponseEntity.ok(positionService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        positionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}