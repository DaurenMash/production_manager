package com.prodman.employeeservice.service;

import com.prodman.employeeservice.dto.request.CreateEmployeeRequest;
import com.prodman.employeeservice.dto.request.UpdateEmployeeRequest;
import com.prodman.employeeservice.dto.response.EmployeeResponse;
import com.prodman.employeeservice.model.Department;
import com.prodman.employeeservice.model.Employee;
import com.prodman.employeeservice.model.Position;
import com.prodman.employeeservice.repository.DepartmentRepository;
import com.prodman.employeeservice.repository.EmployeeRepository;
import com.prodman.employeeservice.repository.PositionRepository;
import com.prodman.employeeservice.tenant.TenantContext;
import com.prodman.employeeservice.util.PhoneUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> list(Pageable pageable) {
        UUID tenantId = TenantContext.require();
        return employeeRepository.findAllByTenantId(tenantId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse get(UUID id) {
        UUID tenantId = TenantContext.require();
        Employee e = employeeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));
        return toResponse(e);
    }

    @Transactional
    public EmployeeResponse create(CreateEmployeeRequest req) {
        UUID tenantId = TenantContext.require();
        String phoneE164 = PhoneUtils.toE164(req.getPhone());

        if (employeeRepository.existsByTenantIdAndCode(tenantId, req.getCode())) {
            throw new IllegalArgumentException("Employee code already exists: " + req.getCode());
        }
        if (employeeRepository.existsByTenantIdAndPhone(tenantId, phoneE164)) {
            throw new IllegalArgumentException("Employee phone already exists: " + phoneE164);
        }

        Department department = resolveDepartment(req.getDepartmentId(), tenantId);
        Position position = resolvePosition(req.getPositionId(), tenantId);

        Employee e = Employee.builder()
                .tenantId(tenantId)
                .code(req.getCode())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .middleName(req.getMiddleName())
                .phone(phoneE164)
                .hiredAt(req.getHiredAt())
                .firedAt(req.getFiredAt())
                .department(department)
                .position(position)
                .userId(req.getUserId())
                .maxConsecutiveHours(req.getMaxConsecutiveHours() == null ? 12 : req.getMaxConsecutiveHours())
                .build();

        return toResponse(employeeRepository.save(e));
    }

    @Transactional
    public EmployeeResponse update(UUID id, UpdateEmployeeRequest req) {
        UUID tenantId = TenantContext.require();
        Employee e = employeeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));

        String phoneE164 = PhoneUtils.toE164(req.getPhone());
        if (employeeRepository.existsByTenantIdAndCodeAndIdNot(tenantId, req.getCode(), id)) {
            throw new IllegalArgumentException("Employee code already exists: " + req.getCode());
        }
        if (employeeRepository.existsByTenantIdAndPhoneAndIdNot(tenantId, phoneE164, id)) {
            throw new IllegalArgumentException("Employee phone already exists: " + phoneE164);
        }

        e.setCode(req.getCode());
        e.setFirstName(req.getFirstName());
        e.setLastName(req.getLastName());
        e.setMiddleName(req.getMiddleName());
        e.setPhone(phoneE164);
        e.setHiredAt(req.getHiredAt());
        e.setFiredAt(req.getFiredAt());
        e.setDepartment(resolveDepartment(req.getDepartmentId(), tenantId));
        e.setPosition(resolvePosition(req.getPositionId(), tenantId));
        e.setUserId(req.getUserId());
        if (req.getMaxConsecutiveHours() != null) {
            e.setMaxConsecutiveHours(req.getMaxConsecutiveHours());
        }
        return toResponse(employeeRepository.save(e));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = TenantContext.require();
        Employee e = employeeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));
        employeeRepository.delete(e);
    }

    private Department resolveDepartment(UUID departmentId, UUID tenantId) {
        if (departmentId == null) return null;
        return departmentRepository.findByIdAndTenantId(departmentId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Department not found in this tenant: " + departmentId));
    }

    private Position resolvePosition(UUID positionId, UUID tenantId) {
        if (positionId == null) return null;
        return positionRepository.findByIdAndTenantId(positionId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Position not found in this tenant: " + positionId));
    }

    private EmployeeResponse toResponse(Employee e) {
        return EmployeeResponse.builder()
                .id(e.getId())
                .code(e.getCode())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .middleName(e.getMiddleName())
                .phone(e.getPhone())
                .hiredAt(e.getHiredAt())
                .firedAt(e.getFiredAt())
                .status(e.getStatus())
                .departmentId(e.getDepartment() == null ? null : e.getDepartment().getId())
                .departmentName(e.getDepartment() == null ? null : e.getDepartment().getName())
                .positionId(e.getPosition() == null ? null : e.getPosition().getId())
                .positionName(e.getPosition() == null ? null : e.getPosition().getName())
                .userId(e.getUserId())
                .maxConsecutiveHours(e.getMaxConsecutiveHours())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}