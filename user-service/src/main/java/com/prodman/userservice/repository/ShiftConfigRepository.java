package com.prodman.userservice.repository;

import com.prodman.userservice.model.ShiftConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftConfigRepository extends JpaRepository<ShiftConfig, String> {
    Optional<ShiftConfig> findByIsActiveTrue();
    List<ShiftConfig> findAllByOrderByCreatedAtDesc();
}