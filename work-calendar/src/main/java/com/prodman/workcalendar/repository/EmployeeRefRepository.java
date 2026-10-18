package com.prodman.workcalendar.repository;

import com.prodman.workcalendar.model.EmployeeRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRefRepository extends JpaRepository<EmployeeRef, UUID> {

    List<EmployeeRef> findAllByTenantId(UUID tenantId);

    Optional<EmployeeRef> findByIdAndTenantId(UUID id, UUID tenantId);

    void deleteAllByTenantId(UUID tenantId);
}