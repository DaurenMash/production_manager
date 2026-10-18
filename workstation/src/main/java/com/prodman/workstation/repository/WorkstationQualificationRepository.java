package com.prodman.workstation.repository;

import com.prodman.workstation.model.WorkstationQualification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkstationQualificationRepository
        extends JpaRepository<WorkstationQualification, UUID> {

    List<WorkstationQualification> findAllByTenantIdAndWorkstationId(UUID tenantId, UUID workstationId);

    void deleteAllByTenantIdAndWorkstationId(UUID tenantId, UUID workstationId);

    List<WorkstationQualification> findAllByTenantId(UUID tenantId);
}