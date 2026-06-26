package com.prodman.workcalendar.repository;

import com.prodman.workcalendar.model.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, String> {
    List<Shift> findByEmployeeId(String employeeId);
    
    List<Shift> findByEmployeeIdAndDateBetween(String employeeId, LocalDate start, LocalDate end);
    
    List<Shift> findByDate(LocalDate date);
    
    List<Shift> findByDateBetween(LocalDate start, LocalDate end);
    
    List<Shift> findByEmployeeIdAndAvailableTrue(String employeeId);
    
    List<Shift> findByEmployeeIdAndDateAndAvailableTrue(String employeeId, LocalDate date);
}