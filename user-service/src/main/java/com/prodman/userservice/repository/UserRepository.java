package com.prodman.userservice.repository;

import com.prodman.userservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Логин: ищем пользователя по (tenant_id, username).
     * tenantId обязателен — мы не позволяем логин без явного тенанта.
     */
    Optional<User> findByTenantIdAndUsername(UUID tenantId, String username);

    /**
     * Суперадмин платформы: tenant_id = null.
     */
    Optional<User> findByTenantIdIsNullAndUsername(String username);

    /**
     * Поиск для суперадмина платформы (или для внутренних задач).
     */
    Optional<User> findByTenantIdIsNullAndEmail(String email);

    Page<User> findAllByTenantId(UUID tenantId, Pageable pageable);

    Optional<User> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByTenantIdAndUsername(UUID tenantId, String username);

    boolean existsByTenantIdAndUsernameAndIdNot(UUID tenantId, String username, UUID id);

    boolean existsByTenantIdAndEmail(UUID tenantId, String email);

    boolean existsByTenantIdAndEmailAndIdNot(UUID tenantId, String email, UUID id);

    boolean existsByTenantIdIsNullAndUsername(String username);

    boolean existsByTenantIdIsNullAndEmail(String email);
}