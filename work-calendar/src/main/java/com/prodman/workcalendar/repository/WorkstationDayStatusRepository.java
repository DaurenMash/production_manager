package com.prodman.workcalendar.repository;

import com.prodman.workcalendar.model.WorkstationDayStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkstationDayStatusRepository extends JpaRepository<WorkstationDayStatus, UUID> {

    List<WorkstationDayStatus> findAllByTenantIdAndDateBetween(UUID tenantId, LocalDate from, LocalDate to);

    List<WorkstationDayStatus> findAllByTenantIdAndDate(UUID tenantId, LocalDate date);

    Optional<WorkstationDayStatus> findByTenantIdAndWorkstationIdAndDate(UUID tenantId, UUID workstationId, LocalDate date);

    Optional<WorkstationDayStatus> findByIdAndTenantId(UUID id, UUID tenantId);
}