package com.prodman.userservice.repository;

import com.prodman.userservice.model.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, UUID> {

    List<Shift> findAllByTenantIdAndShiftConfigId(UUID tenantId, UUID shiftConfigId);

    Optional<Shift> findByIdAndTenantId(UUID id, UUID tenantId);
}