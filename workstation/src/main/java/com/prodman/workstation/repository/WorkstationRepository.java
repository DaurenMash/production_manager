package com.prodman.workstation.repository;

import com.prodman.workstation.model.Workstation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkstationRepository extends JpaRepository<Workstation, String> {

    /**
     * Найти все активные рабочие места
     */
    List<Workstation> findByIsActiveTrue();

    /**
     * Найти рабочие места по отделу (без учета регистра)
     */
    List<Workstation> findByDepartmentContainingIgnoreCase(String department);

    /**
     * Найти рабочие места по названию (без учета регистра)
     */
    List<Workstation> findByTitleContainingIgnoreCase(String title);

    /**
     * Найти рабочие места, где работает конкретный сотрудник
     */
    List<Workstation> findByEmployeeIdsContaining(String employeeId);
}