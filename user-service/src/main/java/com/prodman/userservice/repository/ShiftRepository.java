package com.prodman.userservice.repository;

import com.prodman.userservice.model.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, String> {
    List<Shift> findByShiftConfigIdOrderByDisplayOrder(String configId);
}