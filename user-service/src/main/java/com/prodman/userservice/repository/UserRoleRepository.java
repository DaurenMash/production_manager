package com.prodman.userservice.repository;

import com.prodman.userservice.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    List<UserRole> findAllByUserId(UUID userId);
    void deleteByUserIdAndRole(UUID userId, com.prodman.userservice.model.Role role);
}