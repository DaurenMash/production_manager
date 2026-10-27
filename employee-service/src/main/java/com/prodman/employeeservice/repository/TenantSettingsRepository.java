package com.prodman.employeeservice.repository;

import com.prodman.employeeservice.model.TenantSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TenantSettingsRepository extends JpaRepository<TenantSettings, UUID> {
}