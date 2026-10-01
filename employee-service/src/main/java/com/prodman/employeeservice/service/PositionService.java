package com.prodman.employeeservice.service;

import com.prodman.employeeservice.dto.request.CreatePositionRequest;
import com.prodman.employeeservice.dto.request.UpdatePositionRequest;
import com.prodman.employeeservice.dto.response.PositionResponse;
import com.prodman.employeeservice.model.Position;
import com.prodman.employeeservice.repository.PositionRepository;
import com.prodman.employeeservice.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository repository;

    @Transactional(readOnly = true)
    public Page<PositionResponse> list(Pageable pageable) {
        UUID tenantId = TenantContext.require();
        return repository.findAllByTenantId(tenantId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PositionResponse get(UUID id) {
        UUID tenantId = TenantContext.require();
        Position p = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Position not found: " + id));
        return toResponse(p);
    }

    @Transactional
    public PositionResponse create(CreatePositionRequest req) {
        UUID tenantId = TenantContext.require();
        if (repository.existsByTenantIdAndCode(tenantId, req.getCode())) {
            throw new IllegalArgumentException("Position code already exists: " + req.getCode());
        }
        Position p = Position.builder()
                .tenantId(tenantId)
                .code(req.getCode())
                .name(req.getName())
                .description(req.getDescription())
                .isActive(req.getIsActive() == null ? Boolean.TRUE : req.getIsActive())
                .build();
        return toResponse(repository.save(p));
    }

    @Transactional
    public PositionResponse update(UUID id, UpdatePositionRequest req) {
        UUID tenantId = TenantContext.require();
        Position p = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Position not found: " + id));
        if (repository.existsByTenantIdAndCodeAndIdNot(tenantId, req.getCode(), id)) {
            throw new IllegalArgumentException("Position code already exists: " + req.getCode());
        }
        p.setCode(req.getCode());
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        if (req.getIsActive() != null) {
            p.setIsActive(req.getIsActive());
        }
        return toResponse(repository.save(p));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = TenantContext.require();
        Position p = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Position not found: " + id));
        repository.delete(p);
    }

    private PositionResponse toResponse(Position p) {
        return PositionResponse.builder()
                .id(p.getId())
                .code(p.getCode())
                .name(p.getName())
                .description(p.getDescription())
                .isActive(p.getIsActive())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}