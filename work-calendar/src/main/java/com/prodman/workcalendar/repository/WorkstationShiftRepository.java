package com.prodman.workcalendar.repository;

import com.prodman.workcalendar.model.WorkstationShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkstationShiftRepository extends JpaRepository<WorkstationShift, UUID> {

    List<WorkstationShift> findAllByTenantIdAndDateBetween(UUID tenantId, LocalDate from, LocalDate to);

    List<WorkstationShift> findAllByTenantIdAndDate(UUID tenantId, LocalDate date);

    List<WorkstationShift> findAllByTenantIdAndWorkstationIdAndDate(UUID tenantId, UUID workstationId, LocalDate date);

    Optional<WorkstationShift> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<WorkstationShift> findByTenantIdAndWorkstationIdAndDateAndShiftPatternId(
            UUID tenantId, UUID workstationId, LocalDate date, UUID shiftPatternId);
}