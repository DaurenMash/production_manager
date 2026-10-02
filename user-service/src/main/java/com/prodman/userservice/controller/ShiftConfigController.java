package com.prodman.userservice.controller;

import com.prodman.userservice.dto.request.CreateShiftConfigRequest;
import com.prodman.userservice.dto.response.ShiftConfigResponse;
import com.prodman.userservice.service.ShiftConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shift-configs")
@RequiredArgsConstructor
public class ShiftConfigController {

    private final ShiftConfigService shiftConfigService;

    @PostMapping
    public ResponseEntity<ShiftConfigResponse> createConfig(@Valid @RequestBody CreateShiftConfigRequest request) {
        return ResponseEntity.ok(shiftConfigService.createShiftConfig(request));
    }

    @GetMapping("/active")
    public ResponseEntity<ShiftConfigResponse> getActiveConfig() {
        return ResponseEntity.ok(shiftConfigService.getActiveConfig());
    }

    @GetMapping
    public ResponseEntity<List<ShiftConfigResponse>> getAllConfigs() {
        return ResponseEntity.ok(shiftConfigService.getAllConfigs());
    }

    @GetMapping("/{id}/shifts")
    public ResponseEntity<List<String>> getShiftNames(@PathVariable UUID id) {
        return ResponseEntity.ok(shiftConfigService.getShiftNamesForConfig(id));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ShiftConfigResponse> activateConfig(@PathVariable UUID id) {
        return ResponseEntity.ok(shiftConfigService.setActiveConfig(id));
    }
}