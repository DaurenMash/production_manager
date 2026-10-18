package com.prodman.workcalendar.repository;

import com.prodman.workcalendar.model.WorkstationRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkstationRefRepository extends JpaRepository<WorkstationRef, UUID> {

    List<WorkstationRef> findAllByTenantId(UUID tenantId);

    List<WorkstationRef> findAllByTenantIdAndDepartmentId(UUID tenantId, UUID departmentId);

    List<WorkstationRef> findAllByTenantIdAndIsActive(UUID tenantId, Boolean isActive);

    Optional<WorkstationRef> findByIdAndTenantId(UUID id, UUID tenantId);

    void deleteAllByTenantId(UUID tenantId);
}