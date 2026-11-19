package com.prodman.userservice.controller;

import com.prodman.userservice.dto.response.AuthResponse;
import com.prodman.userservice.dto.response.TenantResponse;
import com.prodman.userservice.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @GetMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<Page<TenantResponse>> list(
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(tenantService.listAll(pageable));
    }

    /**
     * Имперсонация: PLATFORM_ADMIN получает AuthResponse с role=ADMIN и tenantId=<id>,
     * чтобы фронт мог «войти» в контекст тенанта.
     */
    @PostMapping("/{id}/impersonate")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<AuthResponse> impersonate(@PathVariable UUID id) {
        return ResponseEntity.ok(tenantService.impersonate(id));
    }
}