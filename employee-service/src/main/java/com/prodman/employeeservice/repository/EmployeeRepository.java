package com.prodman.employeeservice.repository;

import com.prodman.employeeservice.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Page<Employee> findAllByTenantId(UUID tenantId, Pageable pageable);

    Page<Employee> findAllByTenantIdAndDepartmentId(UUID tenantId, UUID departmentId, Pageable pageable);

    Optional<Employee> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Employee> findByTenantIdAndPhone(UUID tenantId, String phone);

    boolean existsByTenantIdAndCode(UUID tenantId, String code);

    boolean existsByTenantIdAndCodeAndIdNot(UUID tenantId, String code, UUID id);

    boolean existsByTenantIdAndPhone(UUID tenantId, String phone);

    boolean existsByTenantIdAndPhoneAndIdNot(UUID tenantId, String phone, UUID id);
}