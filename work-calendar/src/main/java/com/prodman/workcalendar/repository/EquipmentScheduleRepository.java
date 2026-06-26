package com.prodman.workcalendar.repository;

import com.prodman.workcalendar.model.EquipmentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EquipmentScheduleRepository extends JpaRepository<EquipmentSchedule, String> {
    List<EquipmentSchedule> findByDate(LocalDate date);
    
    List<EquipmentSchedule> findByDateBetween(LocalDate start, LocalDate end);
    
    List<EquipmentSchedule> findByEquipmentId(String equipmentId);
    
    List<EquipmentSchedule> findByEquipmentIdAndDateBetween(String equipmentId, LocalDate start, LocalDate end);
}