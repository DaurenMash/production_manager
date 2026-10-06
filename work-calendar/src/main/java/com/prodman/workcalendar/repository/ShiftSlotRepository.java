package com.prodman.workcalendar.repository;

import com.prodman.workcalendar.model.ShiftSlot;
import com.prodman.workcalendar.model.ShiftSlotStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShiftSlotRepository extends JpaRepository<ShiftSlot, UUID> {

    Page<ShiftSlot> findAllByTenantId(UUID tenantId, Pageable pageable);

    Optional<ShiftSlot> findByIdAndTenantId(UUID id, UUID tenantId);

    List<ShiftSlot> findAllByTenantIdAndDateBetween(UUID tenantId, LocalDate from, LocalDate to);

    List<ShiftSlot> findAllByTenantIdAndDate(UUID tenantId, LocalDate date);

    List<ShiftSlot> findAllByTenantIdAndWorkstationIdAndDate(UUID tenantId, UUID workstationId, LocalDate date);

    List<ShiftSlot> findAllByTenantIdAndEmployeeIdAndDateAndStatusNot(
            UUID tenantId, UUID employeeId, LocalDate date, ShiftSlotStatus status);

    /** Слоты сотрудника за период (для отчёта по сотруднику). */
    List<ShiftSlot> findAllByTenantIdAndEmployeeIdAndDateBetween(
            UUID tenantId, UUID employeeId, LocalDate from, LocalDate to);

    /** Агрегат по сотрудникам за период. */
    @Query("""
        select s.employeeId, s.employeeFullName, s.employeeDepartmentId,
               coalesce(sum(coalesce(s.actualHours, s.plannedHours)), 0),
               coalesce(sum(coalesce(s.actualNightHours, s.plannedNightHours)), 0),
               count(s)
        from ShiftSlot s
        where s.tenantId = :tenantId
          and s.date between :from and :to
          and s.employeeId is not null
          and s.status = com.prodman.workcalendar.model.ShiftSlotStatus.FILLED
        group by s.employeeId, s.employeeFullName, s.employeeDepartmentId
        order by s.employeeFullName
    """)
    List<Object[]> aggregateHoursByEmployee(
            @Param("tenantId") UUID tenantId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    /** Агрегат по отделам за период. */
    @Query("""
        select s.employeeDepartmentId,
               coalesce(sum(coalesce(s.actualHours, s.plannedHours)), 0),
               coalesce(sum(coalesce(s.actualNightHours, s.plannedNightHours)), 0),
               count(s)
        from ShiftSlot s
        where s.tenantId = :tenantId
          and s.date between :from and :to
          and s.employeeId is not null
          and s.status = com.prodman.workcalendar.model.ShiftSlotStatus.FILLED
        group by s.employeeDepartmentId
    """)
    List<Object[]> aggregateHoursByDepartment(
            @Param("tenantId") UUID tenantId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}