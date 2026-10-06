package com.prodman.workstation.service;

import com.prodman.workstation.dto.request.CreateWorkstationRequest;
import com.prodman.workstation.dto.request.UpdateWorkstationRequest;
import com.prodman.workstation.dto.response.WorkstationResponse;
import com.prodman.workstation.model.Workstation;
import com.prodman.workstation.repository.WorkstationRepository;
import com.prodman.workstation.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkstationService {

    private final WorkstationRepository repository;

    @Transactional(readOnly = true)
    public Page<WorkstationResponse> list(Pageable pageable) {
        UUID tenantId = TenantContext.require();
        return repository.findAllByTenantId(tenantId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<WorkstationResponse> listByDepartment(UUID departmentId, Pageable pageable) {
        UUID tenantId = TenantContext.require();
        return repository.findAllByTenantIdAndDepartmentId(tenantId, departmentId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public WorkstationResponse get(UUID id) {
        UUID tenantId = TenantContext.require();
        Workstation w = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Workstation not found: " + id));
        return toResponse(w);
    }

    @Transactional
    public WorkstationResponse create(CreateWorkstationRequest req) {
        UUID tenantId = TenantContext.require();
        if (repository.existsByTenantIdAndCode(tenantId, req.getCode())) {
            throw new IllegalArgumentException("Workstation code already exists: " + req.getCode());
        }
        Workstation w = Workstation.builder()
                .tenantId(tenantId)
                .departmentId(req.getDepartmentId())
                .requiredQualificationId(req.getRequiredQualificationId())
                .code(req.getCode())
                .name(req.getName())
                .description(req.getDescription())
                .isActive(req.getIsActive() == null ? Boolean.TRUE : req.getIsActive())
                .build();
        return toResponse(repository.save(w));
    }

    @Transactional
    public WorkstationResponse update(UUID id, UpdateWorkstationRequest req) {
        UUID tenantId = TenantContext.require();
        Workstation w = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Workstation not found: " + id));
        if (repository.existsByTenantIdAndCodeAndIdNot(tenantId, req.getCode(), id)) {
            throw new IllegalArgumentException("Workstation code already exists: " + req.getCode());
        }
        w.setDepartmentId(req.getDepartmentId());
        w.setRequiredQualificationId(req.getRequiredQualificationId());
        w.setCode(req.getCode());
        w.setName(req.getName());
        w.setDescription(req.getDescription());
        if (req.getIsActive() != null) w.setIsActive(req.getIsActive());
        return toResponse(repository.save(w));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = TenantContext.require();
        Workstation w = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Workstation not found: " + id));
        repository.delete(w);
    }

    private WorkstationResponse toResponse(Workstation w) {
        return WorkstationResponse.builder()
                .id(w.getId())
                .departmentId(w.getDepartmentId())
                .requiredQualificationId(w.getRequiredQualificationId())
                .code(w.getCode())
                .name(w.getName())
                .description(w.getDescription())
                .isActive(w.getIsActive())
                .createdBy(w.getCreatedBy())
                .createdAt(w.getCreatedAt())
                .updatedAt(w.getUpdatedAt())
                .build();
    }
}