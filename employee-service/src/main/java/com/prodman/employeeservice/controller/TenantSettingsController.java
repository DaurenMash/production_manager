package com.prodman.employeeservice.controller;

import com.prodman.employeeservice.dto.request.UpdateTenantSettingsRequest;
import com.prodman.employeeservice.dto.response.TenantSettingsResponse;
import com.prodman.employeeservice.service.TenantSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/tenant-settings")
@RequiredArgsConstructor
public class TenantSettingsController {

    private final TenantSettingsService service;

    @GetMapping
    public ResponseEntity<TenantSettingsResponse> get() {
        return ResponseEntity.ok(service.get());
    }

    @PutMapping
    public ResponseEntity<TenantSettingsResponse> update(@Valid @RequestBody UpdateTenantSettingsRequest req) {
        return ResponseEntity.ok(service.update(req));
    }
}