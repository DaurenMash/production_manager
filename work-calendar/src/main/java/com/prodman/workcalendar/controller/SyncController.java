package com.prodman.workcalendar.controller;

import com.prodman.workcalendar.service.RefSyncService;
import com.prodman.workcalendar.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {

    private final RefSyncService syncService;

    @PostMapping("/refs")
    public ResponseEntity<Map<String, Integer>> sync() {
        UUID tenantId = TenantContext.require();
        int ws = syncService.syncWorkstations(tenantId);
        int emp = syncService.syncEmployees(tenantId);
        return ResponseEntity.ok(Map.of("workstations", ws, "employees", emp));
    }
}