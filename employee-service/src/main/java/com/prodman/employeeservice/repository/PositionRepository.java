package com.prodman.employeeservice.repository;

import com.prodman.employeeservice.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, String> {

    Optional<Position> findByName(String name);

    boolean existsByName(String name);
}