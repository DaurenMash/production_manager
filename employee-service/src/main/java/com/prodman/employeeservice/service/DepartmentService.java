package com.prodman.employeeservice.service;

import com.prodman.employeeservice.dto.request.CreateDepartmentRequest;
import com.prodman.employeeservice.dto.request.UpdateDepartmentRequest;
import com.prodman.employeeservice.dto.response.DepartmentResponse;
import com.prodman.employeeservice.model.Department;
import com.prodman.employeeservice.repository.DepartmentRepository;
import com.prodman.employeeservice.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository repository;

    @Transactional(readOnly = true)
    public Page<DepartmentResponse> list(Pageable pageable) {
        UUID tenantId = TenantContext.require();
        return repository.findAllByTenantId(tenantId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public DepartmentResponse get(UUID id) {
        UUID tenantId = TenantContext.require();
        Department d = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + id));
        return toResponse(d);
    }

    @Transactional
    public DepartmentResponse create(CreateDepartmentRequest req) {
        UUID tenantId = TenantContext.require();
        if (repository.existsByTenantIdAndCode(tenantId, req.getCode())) {
            throw new IllegalArgumentException("Department code already exists: " + req.getCode());
        }
        Department d = Department.builder()
                .tenantId(tenantId)
                .code(req.getCode())
                .name(req.getName())
                .description(req.getDescription())
                .isActive(req.getIsActive() == null ? Boolean.TRUE : req.getIsActive())
                .build();
        return toResponse(repository.save(d));
    }

    @Transactional
    public DepartmentResponse update(UUID id, UpdateDepartmentRequest req) {
        UUID tenantId = TenantContext.require();
        Department d = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + id));
        if (repository.existsByTenantIdAndCodeAndIdNot(tenantId, req.getCode(), id)) {
            throw new IllegalArgumentException("Department code already exists: " + req.getCode());
        }
        d.setCode(req.getCode());
        d.setName(req.getName());
        d.setDescription(req.getDescription());
        if (req.getIsActive() != null) {
            d.setIsActive(req.getIsActive());
        }
        return toResponse(repository.save(d));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = TenantContext.require();
        Department d = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + id));
        repository.delete(d);
    }

    private DepartmentResponse toResponse(Department d) {
        return DepartmentResponse.builder()
                .id(d.getId())
                .code(d.getCode())
                .name(d.getName())
                .description(d.getDescription())
                .isActive(d.getIsActive())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}