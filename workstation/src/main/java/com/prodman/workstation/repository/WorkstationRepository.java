package com.prodman.workstation.repository;

import com.prodman.workstation.model.Workstation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkstationRepository extends JpaRepository<Workstation, UUID> {

    Page<Workstation> findAllByTenantId(UUID tenantId, Pageable pageable);

    Page<Workstation> findAllByTenantIdAndDepartmentId(UUID tenantId, UUID departmentId, Pageable pageable);

    Optional<Workstation> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByTenantIdAndCode(UUID tenantId, String code);

    boolean existsByTenantIdAndCodeAndIdNot(UUID tenantId, String code, UUID id);
}