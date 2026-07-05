package com.prodman.employeeservice.repository;

import com.prodman.employeeservice.model.Employee;
import com.prodman.employeeservice.model.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByUserId(String userId);
    List<Employee> findByStatus(EmployeeStatus status);
    List<Employee> findByDepartment(String department);
    boolean existsByEmail(String email);
    boolean existsByUserId(String userId);
}