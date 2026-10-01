package com.prodman.employeeservice.service;

import com.prodman.employeeservice.dto.request.CreateQualificationRequest;
import com.prodman.employeeservice.dto.request.UpdateQualificationRequest;
import com.prodman.employeeservice.dto.response.QualificationResponse;
import com.prodman.employeeservice.model.Qualification;
import com.prodman.employeeservice.repository.QualificationRepository;
import com.prodman.employeeservice.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QualificationService {

    private final QualificationRepository repository;

    @Transactional(readOnly = true)
    public Page<QualificationResponse> list(Pageable pageable) {
        UUID tenantId = TenantContext.require();
        return repository.findAllByTenantId(tenantId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public QualificationResponse get(UUID id) {
        UUID tenantId = TenantContext.require();
        Qualification q = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Qualification not found: " + id));
        return toResponse(q);
    }

    @Transactional
    public QualificationResponse create(CreateQualificationRequest req) {
        UUID tenantId = TenantContext.require();
        if (repository.existsByTenantIdAndCode(tenantId, req.getCode())) {
            throw new IllegalArgumentException("Qualification code already exists: " + req.getCode());
        }
        Qualification q = Qualification.builder()
                .tenantId(tenantId)
                .code(req.getCode())
                .name(req.getName())
                .description(req.getDescription())
                .isActive(req.getIsActive() == null ? Boolean.TRUE : req.getIsActive())
                .build();
        return toResponse(repository.save(q));
    }

    @Transactional
    public QualificationResponse update(UUID id, UpdateQualificationRequest req) {
        UUID tenantId = TenantContext.require();
        Qualification q = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Qualification not found: " + id));
        if (repository.existsByTenantIdAndCodeAndIdNot(tenantId, req.getCode(), id)) {
            throw new IllegalArgumentException("Qualification code already exists: " + req.getCode());
        }
        q.setCode(req.getCode());
        q.setName(req.getName());
        q.setDescription(req.getDescription());
        if (req.getIsActive() != null) {
            q.setIsActive(req.getIsActive());
        }
        return toResponse(repository.save(q));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = TenantContext.require();
        Qualification q = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Qualification not found: " + id));
        repository.delete(q);
    }

    private QualificationResponse toResponse(Qualification q) {
        return QualificationResponse.builder()
                .id(q.getId())
                .code(q.getCode())
                .name(q.getName())
                .description(q.getDescription())
                .isActive(q.getIsActive())
                .createdAt(q.getCreatedAt())
                .updatedAt(q.getUpdatedAt())
                .build();
    }
}