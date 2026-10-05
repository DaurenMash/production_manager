package com.prodman.employeeservice.repository;

import com.prodman.employeeservice.model.EmployeeQualification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeQualificationRepository extends JpaRepository<EmployeeQualification, UUID> {

    List<EmployeeQualification> findAllByEmployeeId(UUID employeeId);

    Optional<EmployeeQualification> findByIdAndEmployeeTenantId(UUID id, UUID tenantId);

    Optional<EmployeeQualification> findByEmployeeIdAndQualificationId(UUID employeeId, UUID qualificationId);

    boolean existsByEmployeeIdAndQualificationId(UUID employeeId, UUID qualificationId);

    void deleteAllByEmployeeId(UUID employeeId);
}