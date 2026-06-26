package com.prodman.workcalendar.repository;

import com.prodman.workcalendar.model.Employee;
import com.prodman.workcalendar.model.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    List<Employee> findByStatus(EmployeeStatus status);
    
    List<Employee> findByPositionsContaining(String position);
    
    List<Employee> findByStatusAndPositionsContaining(EmployeeStatus status, String position);
    
    List<Employee> findByVacationStartLessThanEqualAndVacationEndGreaterThanEqual(LocalDate date, LocalDate date2);
    
    List<Employee> findBySickLeaveStartLessThanEqualAndSickLeaveEndGreaterThanEqual(LocalDate date, LocalDate date2);
    
    List<Employee> findByStatusNot(EmployeeStatus status);
}