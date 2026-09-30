package com.prodman.employeeservice.controller;

import com.prodman.employeeservice.dto.request.CreatePositionRequest;
import com.prodman.employeeservice.dto.request.UpdatePositionRequest;
import com.prodman.employeeservice.dto.response.PositionResponse;
import com.prodman.employeeservice.service.PositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @PostMapping
    public ResponseEntity<PositionResponse> create(@Valid @RequestBody CreatePositionRequest request) {
        return ResponseEntity.ok(positionService.createPosition(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PositionResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdatePositionRequest request) {
        return ResponseEntity.ok(positionService.updatePosition(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        positionService.deletePosition(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<PositionResponse>> getAll() {
        return ResponseEntity.ok(positionService.getAllPositions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PositionResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(positionService.getPositionById(id));
    }
}