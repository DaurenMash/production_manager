package com.prodman.workcalendar.config;

import com.prodman.workcalendar.repository.EmployeeRefRepository;
import com.prodman.workcalendar.repository.WorkstationRefRepository;
import com.prodman.workcalendar.service.RefSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefSyncOnStartup {

    private final RefSyncService refSyncService;
    private final EmployeeRefRepository employeeRefRepository;
    private final WorkstationRefRepository workstationRefRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void syncAllTenantsOnStartup() {
        // Собираем все tenantId, которые уже есть в ref-таблицах
        Set<UUID> tenants = new HashSet<>();
        employeeRefRepository.findAll().forEach(e -> tenants.add(e.getTenantId()));
        workstationRefRepository.findAll().forEach(w -> tenants.add(w.getTenantId()));

        if (tenants.isEmpty()) {
            log.warn("RefSync on startup: no tenants found in ref tables. Nothing to sync.");
            return;
        }

        for (UUID tenantId : tenants) {
            try {
                log.info("RefSync on startup for tenant {}", tenantId);
                refSyncService.syncAll(tenantId);
            } catch (Exception ex) {
                log.error("RefSync failed for tenant {}: {}", tenantId, ex.getMessage());
            }
        }
    }
}