package com.prodman.workcalendar.service;

import com.prodman.workcalendar.dto.request.AddWorkstationShiftRequest;
import com.prodman.workcalendar.dto.response.WorkstationShiftResponse;
import com.prodman.workcalendar.model.ShiftPattern;
import com.prodman.workcalendar.model.WorkstationShift;
import com.prodman.workcalendar.repository.ShiftPatternRepository;
import com.prodman.workcalendar.repository.WorkstationShiftRepository;
import com.prodman.workcalendar.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkstationShiftService {

    private final WorkstationShiftRepository repository;
    private final ShiftPatternRepository shiftPatternRepository;

    @Transactional(readOnly = true)
    public List<WorkstationShiftResponse> listByDate(LocalDate date) {
        UUID tenantId = TenantContext.require();
        return repository.findAllByTenantIdAndDate(tenantId, date).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public WorkstationShiftResponse add(AddWorkstationShiftRequest req) {
        UUID tenantId = TenantContext.require();
        ShiftPattern pattern = shiftPatternRepository
                .findByIdAndTenantId(req.getShiftPatternId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException("ShiftPattern not found: " + req.getShiftPatternId()));

        // Проверим, что такой уже нет
        WorkstationShift existing = repository
                .findByTenantIdAndWorkstationIdAndDateAndShiftPatternId(
                        tenantId, req.getWorkstationId(), req.getDate(), req.getShiftPatternId())
                .orElse(null);
        if (existing != null) {
            return toResponse(existing);
        }

        WorkstationShift ws = WorkstationShift.builder()
                .tenantId(tenantId)
                .workstationId(req.getWorkstationId())
                .date(req.getDate())
                .shiftPatternId(pattern.getId())
                .build();
        return toResponse(repository.save(ws));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = TenantContext.require();
        WorkstationShift ws = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("WorkstationShift not found: " + id));
        repository.delete(ws);
    }

    private WorkstationShiftResponse toResponse(WorkstationShift ws) {
        String name = shiftPatternRepository.findById(ws.getShiftPatternId())
                .map(ShiftPattern::getName).orElse(null);
        return WorkstationShiftResponse.builder()
                .id(ws.getId())
                .workstationId(ws.getWorkstationId())
                .date(ws.getDate())
                .shiftPatternId(ws.getShiftPatternId())
                .shiftPatternName(name)
                .createdAt(ws.getCreatedAt())
                .updatedAt(ws.getUpdatedAt())
                .build();
    }
}