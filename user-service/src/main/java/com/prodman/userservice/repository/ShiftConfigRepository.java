package com.prodman.userservice.repository;

import com.prodman.userservice.model.ShiftConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShiftConfigRepository extends JpaRepository<ShiftConfig, UUID> {

    List<ShiftConfig> findAllByTenantId(UUID tenantId);

    List<ShiftConfig> findAllByTenantIdAndIsActive(UUID tenantId, Boolean isActive);

    Optional<ShiftConfig> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<ShiftConfig> findFirstByTenantIdAndIsActive(UUID tenantId, Boolean isActive);
}