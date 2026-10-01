package com.prodman.employeeservice.service;

import com.prodman.employeeservice.model.Employee;
import com.prodman.employeeservice.model.EmployeeQualification;
import com.prodman.employeeservice.model.Qualification;
import com.prodman.employeeservice.repository.EmployeeQualificationRepository;
import com.prodman.employeeservice.repository.EmployeeRepository;
import com.prodman.employeeservice.repository.QualificationRepository;
import com.prodman.employeeservice.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeQualificationService {

    private final EmployeeQualificationRepository repository;
    private final EmployeeRepository employeeRepository;
    private final QualificationRepository qualificationRepository;

    @Transactional(readOnly = true)
    public List<EmployeeQualification> listForEmployee(UUID employeeId) {
        UUID tenantId = TenantContext.require();
        employeeRepository.findByIdAndTenantId(employeeId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));
        return repository.findAllByEmployeeId(employeeId);
    }

    @Transactional
    public EmployeeQualification assign(UUID employeeId,
                                        UUID qualificationId,
                                        Integer level,
                                        UUID assignedBy,
                                        String notes) {
        UUID tenantId = TenantContext.require();
        Employee employee = employeeRepository.findByIdAndTenantId(employeeId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));
        Qualification qualification = qualificationRepository.findByIdAndTenantId(qualificationId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Qualification not found: " + qualificationId));

        if (repository.existsByEmployeeIdAndQualificationId(employeeId, qualificationId)) {
            throw new IllegalArgumentException("Qualification already assigned to employee");
        }

        EmployeeQualification eq = EmployeeQualification.builder()
                .employee(employee)
                .qualification(qualification)
                .level(level == null ? 1 : level)
                .assignedAt(LocalDate.now())
                .assignedBy(assignedBy)
                .notes(notes)
                .build();
        return repository.save(eq);
    }

    @Transactional
    public void revoke(UUID employeeId, UUID qualificationId) {
        UUID tenantId = TenantContext.require();
        employeeRepository.findByIdAndTenantId(employeeId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));
        repository.findByEmployeeIdAndQualificationId(employeeId, qualificationId)
                .ifPresent(repository::delete);
    }
}