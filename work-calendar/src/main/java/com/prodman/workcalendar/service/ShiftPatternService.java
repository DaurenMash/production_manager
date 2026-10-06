package com.prodman.workcalendar.service;

import com.prodman.workcalendar.dto.request.CreateShiftPatternRequest;
import com.prodman.workcalendar.dto.request.UpdateShiftPatternRequest;
import com.prodman.workcalendar.dto.response.ShiftPatternResponse;
import com.prodman.workcalendar.model.ShiftPattern;
import com.prodman.workcalendar.repository.ShiftPatternRepository;
import com.prodman.workcalendar.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShiftPatternService {

    private final ShiftPatternRepository repository;

    @Transactional(readOnly = true)
    public Page<ShiftPatternResponse> list(Pageable pageable) {
        UUID tenantId = TenantContext.require();
        return repository.findAllByTenantId(tenantId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ShiftPatternResponse get(UUID id) {
        UUID tenantId = TenantContext.require();
        ShiftPattern p = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("ShiftPattern not found: " + id));
        return toResponse(p);
    }

    @Transactional
    public ShiftPatternResponse create(CreateShiftPatternRequest req) {
        UUID tenantId = TenantContext.require();
        if (repository.existsByTenantIdAndCode(tenantId, req.getCode())) {
            throw new IllegalArgumentException("ShiftPattern code already exists: " + req.getCode());
        }
        ShiftPattern p = ShiftPattern.builder()
                .tenantId(tenantId)
                .code(req.getCode())
                .name(req.getName())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .crossesMidnight(req.getCrossesMidnight() == null
                        ? computeCrossesMidnight(req.getStartTime(), req.getEndTime())
                        : req.getCrossesMidnight())
                .totalHours(req.getTotalHours())
                .nightHours(req.getNightHours() == null ? BigDecimal.ZERO : req.getNightHours())
                .nightWindowStart(req.getNightWindowStart())
                .nightWindowEnd(req.getNightWindowEnd())
                .coefficient(req.getCoefficient() == null ? BigDecimal.ONE : req.getCoefficient())
                .isActive(req.getIsActive() == null ? Boolean.TRUE : req.getIsActive())
                .build();
        validateHours(p);
        return toResponse(repository.save(p));
    }

    @Transactional
    public ShiftPatternResponse update(UUID id, UpdateShiftPatternRequest req) {
        UUID tenantId = TenantContext.require();
        ShiftPattern p = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("ShiftPattern not found: " + id));
        if (repository.existsByTenantIdAndCodeAndIdNot(tenantId, req.getCode(), id)) {
            throw new IllegalArgumentException("ShiftPattern code already exists: " + req.getCode());
        }
        p.setCode(req.getCode());
        p.setName(req.getName());
        p.setStartTime(req.getStartTime());
        p.setEndTime(req.getEndTime());
        p.setCrossesMidnight(req.getCrossesMidnight() == null
                ? computeCrossesMidnight(req.getStartTime(), req.getEndTime())
                : req.getCrossesMidnight());
        p.setTotalHours(req.getTotalHours());
        if (req.getNightHours() != null) p.setNightHours(req.getNightHours());
        p.setNightWindowStart(req.getNightWindowStart());
        p.setNightWindowEnd(req.getNightWindowEnd());
        if (req.getCoefficient() != null) p.setCoefficient(req.getCoefficient());
        if (req.getIsActive() != null) p.setIsActive(req.getIsActive());
        validateHours(p);
        return toResponse(repository.save(p));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = TenantContext.require();
        ShiftPattern p = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("ShiftPattern not found: " + id));
        repository.delete(p);
    }

    private boolean computeCrossesMidnight(LocalTime start, LocalTime end) {
        if (start == null || end == null) return false;
        return !end.isAfter(start);
    }

    private void validateHours(ShiftPattern p) {
        if (p.getNightHours() != null && p.getTotalHours() != null
                && p.getNightHours().compareTo(p.getTotalHours()) > 0) {
            throw new IllegalArgumentException("nightHours cannot exceed totalHours");
        }
    }

    private ShiftPatternResponse toResponse(ShiftPattern p) {
        return ShiftPatternResponse.builder()
                .id(p.getId())
                .code(p.getCode())
                .name(p.getName())
                .startTime(p.getStartTime())
                .endTime(p.getEndTime())
                .crossesMidnight(p.getCrossesMidnight())
                .totalHours(p.getTotalHours())
                .nightHours(p.getNightHours())
                .nightWindowStart(p.getNightWindowStart())
                .nightWindowEnd(p.getNightWindowEnd())
                .coefficient(p.getCoefficient())
                .isActive(p.getIsActive())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}