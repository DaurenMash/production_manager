package com.prodman.workcalendar.repository;

import com.prodman.workcalendar.model.ShiftPattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShiftPatternRepository extends JpaRepository<ShiftPattern, UUID> {

    Page<ShiftPattern> findAllByTenantId(UUID tenantId, Pageable pageable);

    Optional<ShiftPattern> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByTenantIdAndCode(UUID tenantId, String code);

    boolean existsByTenantIdAndCodeAndIdNot(UUID tenantId, String code, UUID id);
}