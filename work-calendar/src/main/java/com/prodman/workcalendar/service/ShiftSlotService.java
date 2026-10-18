package com.prodman.workcalendar.service;

import com.prodman.workcalendar.dto.request.AssignEmployeeToSlotRequest;
import com.prodman.workcalendar.dto.request.CreateShiftSlotRequest;
import com.prodman.workcalendar.dto.request.UpdateShiftSlotRequest;
import com.prodman.workcalendar.dto.response.ShiftSlotResponse;
import com.prodman.workcalendar.exception.QualificationMismatchException;
import com.prodman.workcalendar.model.EmployeeQualificationRef;
import com.prodman.workcalendar.model.EmployeeRef;
import com.prodman.workcalendar.model.ShiftPattern;
import com.prodman.workcalendar.model.ShiftSlot;
import com.prodman.workcalendar.model.ShiftSlotStatus;
import com.prodman.workcalendar.model.WorkstationRef;
import com.prodman.workcalendar.repository.EmployeeQualificationRefRepository;
import com.prodman.workcalendar.repository.EmployeeRefRepository;
import com.prodman.workcalendar.repository.ShiftPatternRepository;
import com.prodman.workcalendar.repository.ShiftSlotRepository;
import com.prodman.workcalendar.repository.WorkstationRefRepository;
import com.prodman.workcalendar.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftSlotService {

    private final ShiftSlotRepository repository;
    private final ShiftPatternRepository shiftPatternRepository;
    private final EmployeeRefRepository employeeRefRepository;
    private final WorkstationRefRepository workstationRefRepository;
    private final EmployeeQualificationRefRepository employeeQualificationRefRepository;

    @Value("${employee.service.url:http://employee-service-app:8084}")
    private String employeeServiceUrl;

    @Value("${workstation.service.url:http://workstation-app:8083}")
    private String workstationServiceUrl;

    private final RestClient restClient = RestClient.create();

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

        String wsName = resolveWorkstationName(req.getWorkstationId(), tenantId);

        ShiftSlot s = ShiftSlot.builder()
                .tenantId(tenantId)
                .date(req.getDate())
                .workstationId(req.getWorkstationId())
                .workstationName(wsName)
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

        // 1) Проверка: у сотрудника нет другой смены в этот день?
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

        // 2) Проверка квалификации
        UUID requiredQualId = slot.getRequiredQualificationId();
        if (requiredQualId != null) {
            // Обновляем локальный ref перед проверкой (данные могли измениться в employee-service)
            syncEmployeeQualifications(req.getEmployeeId(), tenantId);
            boolean hasQual = employeeHasQualification(req.getEmployeeId(), requiredQualId, tenantId);
            if (!hasQual) {
                if (req.getForceQualificationOverride() == null || !req.getForceQualificationOverride()) {
                    throw new QualificationMismatchException(requiredQualId);
                }
                if (req.getQualificationOverrideReason() == null
                        || req.getQualificationOverrideReason().isBlank()) {
                    throw new IllegalArgumentException(
                            "qualificationOverrideReason is required when overriding qualification mismatch");
                }
                slot.setQualificationOverridden(true);
                slot.setQualificationOverrideReason(req.getQualificationOverrideReason());
            }
        }

        slot.setEmployeeId(req.getEmployeeId());
        fillEmployeeInfo(slot, req.getEmployeeId(), tenantId);

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
        // Сбрасываем override квалификации
        slot.setQualificationOverridden(false);
        slot.setQualificationOverrideReason(null);
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
        if (req.getIsDayOffWork() != null) slot.setIsDayOffWork(req.getIsDayOffWork());
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

    // ========== helpers ==========

    /**
     * Проверяет, есть ли у сотрудника требуемая квалификация.
     * Fail-open: если employee-service недоступен — считаем, что квалификация ЕСТЬ
     * (не блокируем работу из-за недоступности соседнего сервиса).
     */
    private boolean employeeHasQualification(UUID employeeId, UUID qualificationId, UUID tenantId) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = restClient.get()
                    .uri(employeeServiceUrl + "/api/employees/" + employeeId + "/qualifications")
                    .header("X-Tenant-Id", tenantId.toString())
                    .retrieve()
                    .body(List.class);

            if (list == null) return true;

            for (Map<String, Object> q : list) {
                Object qid = q.get("qualificationId");
                if (qid != null && qualificationId.toString().equals(qid.toString())) {
                    return true;
                }
            }
            return false;
        } catch (Exception ex) {
            log.warn("Failed to check qualification for employee {}: {}", employeeId, ex.getMessage());
            return true; // fail-open
        }
    }

    /**
     * Пытается взять ФИО и departmentId из employees_ref.
     * Если там пусто — идёт в employee-service по REST и кэширует в employees_ref.
     */
    private void fillEmployeeInfo(ShiftSlot slot, UUID employeeId, UUID tenantId) {
        var refOpt = employeeRefRepository.findByIdAndTenantId(employeeId, tenantId);
        if (refOpt.isPresent()) {
            var ref = refOpt.get();
            slot.setEmployeeFullName(ref.getFullName());
            slot.setEmployeeDepartmentId(ref.getDepartmentId());
            if (ref.getFullName() != null) return;
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> resp = restClient.get()
                    .uri(employeeServiceUrl + "/api/employees/" + employeeId)
                    .header("X-Tenant-Id", tenantId.toString())
                    .retrieve()
                    .body(Map.class);

            if (resp == null) return;

            String lastName = (String) resp.get("lastName");
            String firstName = (String) resp.get("firstName");
            String middleName = (String) resp.get("middleName");
            String full = String.join(" ",
                    lastName == null ? "" : lastName,
                    firstName == null ? "" : firstName,
                    middleName == null ? "" : middleName).trim();
            slot.setEmployeeFullName(full.isEmpty() ? null : full);

            Object dep = resp.get("departmentId");
            UUID depId = dep == null ? null : UUID.fromString(dep.toString());
            slot.setEmployeeDepartmentId(depId);

            try {
                EmployeeRef newRef = EmployeeRef.builder()
                        .id(employeeId)
                        .tenantId(tenantId)
                        .fullName(full.isEmpty() ? null : full)
                        .departmentId(depId)
                        .isActive(true)
                        .build();
                employeeRefRepository.save(newRef);
            } catch (Exception ignore) {
                // уникальность/гонки — не критично
            }
        } catch (Exception ex) {
            log.warn("Failed to fetch employee {} from employee-service: {}", employeeId, ex.getMessage());
        }
    }

    /**
     * Пытается взять имя станка из workstations_ref.
     * Если пусто — идёт в workstation по REST и кэширует.
     */
    private String resolveWorkstationName(UUID workstationId, UUID tenantId) {
        var refOpt = workstationRefRepository.findByIdAndTenantId(workstationId, tenantId);
        if (refOpt.isPresent() && refOpt.get().getName() != null) {
            return refOpt.get().getName();
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> resp = restClient.get()
                    .uri(workstationServiceUrl + "/api/workstations/" + workstationId)
                    .header("X-Tenant-Id", tenantId.toString())
                    .retrieve()
                    .body(Map.class);

            if (resp == null) return null;
            String name = (String) resp.get("name");
            String code = (String) resp.get("code");
            Object dep = resp.get("departmentId");
            Object active = resp.get("isActive");

            try {
                WorkstationRef newRef = WorkstationRef.builder()
                        .id(workstationId)
                        .tenantId(tenantId)
                        .code(code)
                        .name(name)
                        .departmentId(dep == null ? null : UUID.fromString(dep.toString()))
                        .isActive(active == null || Boolean.TRUE.equals(active))
                        .build();
                workstationRefRepository.save(newRef);
            } catch (Exception ignore) {
            }

            return name;
        } catch (Exception ex) {
            log.warn("Failed to fetch workstation {} from workstation-service: {}", workstationId, ex.getMessage());
            return null;
        }
    }

    /**
     * Есть ли mismatch: у слота задано требование, есть назначенный сотрудник,
     * и у сотрудника НЕТ этой квалификации в локальном ref-кэше.
     */
    private boolean hasQualificationMismatch(ShiftSlot s, UUID tenantId) {
        if (s.getRequiredQualificationId() == null) return false;
        if (s.getEmployeeId() == null) return false;
        return employeeQualificationRefRepository
                .findAllByTenantIdAndEmployeeId(tenantId, s.getEmployeeId())
                .stream()
                .noneMatch(q -> q.getQualificationId().equals(s.getRequiredQualificationId()));
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
                .qualificationOverridden(s.getQualificationOverridden())
                .qualificationOverrideReason(s.getQualificationOverrideReason())
                .qualificationMismatch(hasQualificationMismatch(s, TenantContext.require()))
                .isDayOffWork(s.getIsDayOffWork())
                .createdBy(s.getCreatedBy())
                .updatedBy(s.getUpdatedBy())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    /**
     * Обновляет локальный кэш квалификаций одного сотрудника из employee-service.
     * Ошибки логируем, но не бросаем — назначение не должно падать из-за недоступности.
     */
    private void syncEmployeeQualifications(UUID employeeId, UUID tenantId) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = restClient.get()
                    .uri(employeeServiceUrl + "/api/employees/" + employeeId + "/qualifications")
                    .header("X-Tenant-Id", tenantId.toString())
                    .retrieve()
                    .body(List.class);

            if (list == null) return;
            LocalDateTime now = LocalDateTime.now();
            for (Map<String, Object> q : list) {
                Object qid = q.get("qualificationId");
                if (qid == null) continue;
                employeeQualificationRefRepository.upsertIgnoreConflict(
                        UUID.randomUUID(),
                        tenantId,
                        employeeId,
                        UUID.fromString(qid.toString()),
                        now);
            }
        } catch (Exception ex) {
            log.warn("Failed to sync qualifications for employee {}: {}", employeeId, ex.getMessage());
        }
    }
}
