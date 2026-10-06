package com.prodman.workcalendar.service;

import com.prodman.workcalendar.dto.request.AssignEmployeeToSlotRequest;
import com.prodman.workcalendar.dto.request.CreateShiftSlotRequest;
import com.prodman.workcalendar.dto.request.UpdateShiftSlotRequest;
import com.prodman.workcalendar.dto.response.ShiftSlotResponse;
import com.prodman.workcalendar.model.*;
import com.prodman.workcalendar.repository.ShiftPatternRepository;
import com.prodman.workcalendar.repository.ShiftSlotRepository;
import com.prodman.workcalendar.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShiftSlotService {

    private final ShiftSlotRepository repository;
    private final ShiftPatternRepository shiftPatternRepository;

    @Transactional(readOnly = true)
    public List<ShiftSlotResponse> listByDate(LocalDate date) {
        UUID tenantId = TenantContext.require();
        return repository.findAllByTenantIdAndDate(tenantId, date).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ShiftSlotResponse> listByWorkstationAndDate(UUID workstationId, LocalDate date) {
        UUID tenantId = TenantContext.require();
        return repository.findAllByTenantIdAndWorkstationIdAndDate(tenantId, workstationId, date).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ShiftSlotResponse get(UUID id) {
        UUID tenantId = TenantContext.require();
        ShiftSlot s = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("ShiftSlot not found: " + id));
        return toResponse(s);
    }

    @Transactional
    public ShiftSlotResponse create(CreateShiftSlotRequest req) {
        UUID tenantId = TenantContext.require();
        ShiftPattern pattern = shiftPatternRepository
                .findByIdAndTenantId(req.getShiftPatternId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException("ShiftPattern not found: " + req.getShiftPatternId()));

        ShiftSlot s = ShiftSlot.builder()
                .tenantId(tenantId)
                .date(req.getDate())
                .workstationId(req.getWorkstationId())
                .shiftPatternId(pattern.getId())
                .shiftPatternName(pattern.getName())
                .requiredQualificationId(req.getRequiredQualificationId())
                .plannedHours(pattern.getTotalHours())
                .plannedNightHours(pattern.getNightHours() == null ? BigDecimal.ZERO : pattern.getNightHours())
                .status(ShiftSlotStatus.OPEN)
                .build();
        return toResponse(repository.save(s));
    }

    @Transactional
    public ShiftSlotResponse assignEmployee(UUID slotId, AssignEmployeeToSlotRequest req) {
        UUID tenantId = TenantContext.require();
        ShiftSlot slot = repository.findByIdAndTenantId(slotId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("ShiftSlot not found: " + slotId));

        // Проверим: у сотрудника нет другой смены в этот день?
        List<ShiftSlot> conflicts = repository
                .findAllByTenantIdAndEmployeeIdAndDateAndStatusNot(
                        tenantId, req.getEmployeeId(), slot.getDate(), ShiftSlotStatus.CANCELLED);
        boolean hasConflict = conflicts.stream().anyMatch(s -> !s.getId().equals(slotId));

        if (hasConflict) {
            if (req.getForce() == null || !req.getForce()) {
                throw new IllegalArgumentException(
                        "Employee already has a shift on this date. Use force=true + overrideReason to override.");
            }
            if (req.getOverrideReason() == null || req.getOverrideReason().isBlank()) {
                throw new IllegalArgumentException("overrideReason is required when overriding");
            }
            slot.setOverridden(true);
            slot.setOverrideReason(req.getOverrideReason());
        }

        slot.setEmployeeId(req.getEmployeeId());
        if (req.getComment() != null) slot.setComment(req.getComment());
        slot.setStatus(ShiftSlotStatus.FILLED);
        return toResponse(repository.save(slot));
    }

    @Transactional
    public ShiftSlotResponse unassignEmployee(UUID slotId) {
        UUID tenantId = TenantContext.require();
        ShiftSlot slot = repository.findByIdAndTenantId(slotId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("ShiftSlot not found: " + slotId));
        slot.setEmployeeId(null);
        slot.setEmployeeFullName(null);
        slot.setEmployeeDepartmentId(null);
        slot.setStatus(ShiftSlotStatus.OPEN);
        return toResponse(repository.save(slot));
    }

    @Transactional
    public ShiftSlotResponse update(UUID slotId, UpdateShiftSlotRequest req) {
        UUID tenantId = TenantContext.require();
        ShiftSlot slot = repository.findByIdAndTenantId(slotId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("ShiftSlot not found: " + slotId));
        if (req.getRequiredQualificationId() != null) slot.setRequiredQualificationId(req.getRequiredQualificationId());
        if (req.getActualHours() != null) slot.setActualHours(req.getActualHours());
        if (req.getActualNightHours() != null) slot.setActualNightHours(req.getActualNightHours());
        if (req.getCoefficientOverride() != null) slot.setCoefficientOverride(req.getCoefficientOverride());
        if (req.getComment() != null) slot.setComment(req.getComment());
        return toResponse(repository.save(slot));
    }

    @Transactional
    public void delete(UUID slotId) {
        UUID tenantId = TenantContext.require();
        ShiftSlot slot = repository.findByIdAndTenantId(slotId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("ShiftSlot not found: " + slotId));
        repository.delete(slot);
    }

    @Transactional(readOnly = true)
    public List<ShiftSlotResponse> listByEmployee(UUID employeeId, LocalDate from, LocalDate to) {
        UUID tenantId = TenantContext.require();
        return repository.findAllByTenantIdAndEmployeeIdAndDateBetween(tenantId, employeeId, from, to)
                .stream().map(this::toResponse).toList();
    }

    private ShiftSlotResponse toResponse(ShiftSlot s) {
        return ShiftSlotResponse.builder()
                .id(s.getId())
                .date(s.getDate())
                .workstationId(s.getWorkstationId())
                .workstationName(s.getWorkstationName())
                .shiftPatternId(s.getShiftPatternId())
                .shiftPatternName(s.getShiftPatternName())
                .requiredQualificationId(s.getRequiredQualificationId())
                .employeeId(s.getEmployeeId())
                .employeeFullName(s.getEmployeeFullName())
                .employeeDepartmentId(s.getEmployeeDepartmentId())
                .plannedHours(s.getPlannedHours())
                .plannedNightHours(s.getPlannedNightHours())
                .actualHours(s.getActualHours())
                .actualNightHours(s.getActualNightHours())
                .coefficientOverride(s.getCoefficientOverride())
                .status(s.getStatus())
                .comment(s.getComment())
                .overridden(s.getOverridden())
                .overrideReason(s.getOverrideReason())
                .createdBy(s.getCreatedBy())
                .updatedBy(s.getUpdatedBy())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}