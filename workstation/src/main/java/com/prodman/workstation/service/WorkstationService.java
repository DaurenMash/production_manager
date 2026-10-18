package com.prodman.workstation.service;

import com.prodman.workstation.dto.request.CreateWorkstationRequest;
import com.prodman.workstation.dto.request.UpdateWorkstationRequest;
import com.prodman.workstation.dto.response.WorkstationResponse;
import com.prodman.workstation.model.Workstation;
import com.prodman.workstation.model.WorkstationQualification;
import com.prodman.workstation.repository.WorkstationQualificationRepository;
import com.prodman.workstation.repository.WorkstationRepository;
import com.prodman.workstation.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkstationService {

    private final WorkstationRepository repository;
    private final WorkstationQualificationRepository qualificationRepository;

    @Transactional(readOnly = true)
    public Page<WorkstationResponse> list(Pageable pageable) {
        UUID tenantId = TenantContext.require();
        Page<Workstation> page = repository.findAllByTenantId(tenantId, pageable);
        Map<UUID, List<UUID>> qualsByWs = loadQualificationsForPage(tenantId, page.getContent());
        return page.map(w -> toResponse(w, qualsByWs.getOrDefault(w.getId(), List.of())));
    }

    @Transactional(readOnly = true)
    public Page<WorkstationResponse> listByDepartment(UUID departmentId, Pageable pageable) {
        UUID tenantId = TenantContext.require();
        Page<Workstation> page = repository.findAllByTenantIdAndDepartmentId(tenantId, departmentId, pageable);
        Map<UUID, List<UUID>> qualsByWs = loadQualificationsForPage(tenantId, page.getContent());
        return page.map(w -> toResponse(w, qualsByWs.getOrDefault(w.getId(), List.of())));
    }

    @Transactional(readOnly = true)
    public WorkstationResponse get(UUID id) {
        UUID tenantId = TenantContext.require();
        Workstation w = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Workstation not found: " + id));
        List<UUID> qualIds = qualificationRepository
                .findAllByTenantIdAndWorkstationId(tenantId, w.getId())
                .stream()
                .map(WorkstationQualification::getQualificationId)
                .toList();
        return toResponse(w, qualIds);
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
                .code(req.getCode())
                .name(req.getName())
                .description(req.getDescription())
                .isActive(req.getIsActive() == null ? Boolean.TRUE : req.getIsActive())
                .build();
        Workstation saved = repository.save(w);
        return toResponse(saved, List.of());
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
        w.setCode(req.getCode());
        w.setName(req.getName());
        w.setDescription(req.getDescription());
        if (req.getIsActive() != null) w.setIsActive(req.getIsActive());
        Workstation saved = repository.save(w);

        List<UUID> qualIds = qualificationRepository
                .findAllByTenantIdAndWorkstationId(tenantId, saved.getId())
                .stream()
                .map(WorkstationQualification::getQualificationId)
                .toList();
        return toResponse(saved, qualIds);
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = TenantContext.require();
        Workstation w = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Workstation not found: " + id));
        qualificationRepository.deleteAllByTenantIdAndWorkstationId(tenantId, w.getId());
        repository.delete(w);
    }

    /**
     * Полностью заменяет набор квалификаций станка.
     * Пустой/нулевой список — очищает.
     */
    @Transactional
    public void setQualifications(UUID workstationId, List<UUID> qualificationIds) {
        UUID tenantId = TenantContext.require();
        Workstation w = repository.findByIdAndTenantId(workstationId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Workstation not found: " + workstationId));

        qualificationRepository.deleteAllByTenantIdAndWorkstationId(tenantId, w.getId());

        if (qualificationIds != null && !qualificationIds.isEmpty()) {
            List<WorkstationQualification> toSave = qualificationIds.stream()
                    .distinct()
                    .map(qId -> WorkstationQualification.builder()
                            .tenantId(tenantId)
                            .workstationId(w.getId())
                            .qualificationId(qId)
                            .build())
                    .toList();
            qualificationRepository.saveAll(toSave);
        }
    }

    // ===== helpers =====

    private Map<UUID, List<UUID>> loadQualificationsForPage(UUID tenantId, List<Workstation> workstations) {
        if (workstations == null || workstations.isEmpty()) return Map.of();
        List<UUID> wsIds = workstations.stream().map(Workstation::getId).toList();
        return qualificationRepository.findAllByTenantId(tenantId).stream()
                .filter(q -> wsIds.contains(q.getWorkstationId()))
                .collect(Collectors.groupingBy(
                        WorkstationQualification::getWorkstationId,
                        Collectors.mapping(WorkstationQualification::getQualificationId, Collectors.toList())));
    }

    private WorkstationResponse toResponse(Workstation w, List<UUID> qualificationIds) {
        return WorkstationResponse.builder()
                .id(w.getId())
                .departmentId(w.getDepartmentId())
                .qualificationIds(qualificationIds)
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