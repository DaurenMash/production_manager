package com.prodman.userservice.repository;

import com.prodman.userservice.model.Tenant;
import com.prodman.userservice.model.TenantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findByIdAndStatus(UUID id, TenantStatus status);

    Optional<Tenant> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    Page<Tenant> findAllByStatus(TenantStatus status, Pageable pageable);

    List<Tenant> findAllByStatus(TenantStatus status);
}