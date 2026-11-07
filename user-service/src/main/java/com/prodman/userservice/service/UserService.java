package com.prodman.userservice.service;

import com.prodman.userservice.dto.request.RegisterRequest;
import com.prodman.userservice.dto.response.UserResponse;
import com.prodman.userservice.exception.CustomException;
import com.prodman.userservice.mapper.UserMapper;
import com.prodman.userservice.model.Role;
import com.prodman.userservice.model.User;
import com.prodman.userservice.model.UserRole;
import com.prodman.userservice.repository.UserRepository;
import com.prodman.userservice.repository.UserRoleRepository;
import com.prodman.userservice.security.JwtAuthenticationFilter;
import com.prodman.userservice.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String ROLE_PREFIX = "ROLE_";

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // ==========================================
    // Проверка прав по иерархии ролей
    // ==========================================

    private Role currentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getAuthorities() == null) {
            return null;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a != null && a.startsWith(ROLE_PREFIX))
                .map(a -> a.substring(ROLE_PREFIX.length()))
                .map(s -> {
                    try {
                        return Role.valueOf(s);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(r -> r != null)
                .max((a, b) -> Integer.compare(a.level(), b.level()))
                .orElse(null);
    }

    private UUID currentUserId() {
        return JwtAuthenticationFilter.currentUserId();
    }

    /**
     * Правило: роль может создать/назначить себе подобного или ниже по иерархии.
     * PLATFORM_ADMIN назначать нельзя никому.
     */
    private void checkCanAssignRole(Role target) {
        Role current = currentRole();
        if (current == null) {
            throw new CustomException.Forbidden("No authenticated role");
        }
        if (target == Role.PLATFORM_ADMIN) {
            throw new CustomException.Forbidden("Cannot assign PLATFORM_ADMIN");
        }
        if (target.level() > current.level()) {
            throw new CustomException.Forbidden(
                    "Role " + current + " cannot assign role " + target);
        }
    }

    /**
     * Может ли текущий пользователь управлять пользователем с ролью target
     * (отзыв роли). То же правило, что и назначение, но строже:
     * нельзя управлять пользователем с такой же ролью (>=).
     */
    private void checkCanManageRole(Role target) {
        Role current = currentRole();
        if (current == null) {
            throw new CustomException.Forbidden("No authenticated role");
        }
        if (target == Role.PLATFORM_ADMIN) {
            throw new CustomException.Forbidden("Cannot manage PLATFORM_ADMIN");
        }
        if (target.level() >= current.level()) {
            throw new CustomException.Forbidden(
                    "Role " + current + " cannot manage user with role " + target);
        }
    }

    // ==========================================
    // CRUD пользователей
    // ==========================================

    @Transactional
    public UserResponse register(RegisterRequest req) {
        UUID tenantId = TenantContext.require();
        Role role = req.getRole() != null ? req.getRole() : Role.TENANT_VISITOR;

        checkCanAssignRole(role);

        // departmentId обязателен для ролей, привязанных к департаменту
        if ((role == Role.HEAD_OF_DEPARTMENT || role == Role.DEPT_VISITOR)
                && req.getDepartmentId() == null) {
            throw new CustomException.BadRequest(
                    "departmentId is required for role " + role);
        }

        if (userRepository.existsByTenantIdAndUsername(tenantId, req.getUsername())) {
            throw new CustomException.Conflict("Username already exists");
        }
        if (userRepository.existsByTenantIdAndEmail(tenantId, req.getEmail())) {
            throw new CustomException.Conflict("Email already exists");
        }

        User user = User.builder()
                .tenantId(tenantId)
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(role)
                .enabled(true)
                .departmentId(req.getDepartmentId())
                .createdBy(currentUserId())
                .createdByRole(currentRole())
                .build();
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> list(Pageable pageable) {
        UUID tenantId = TenantContext.require();
        return userRepository.findAllByTenantId(tenantId, pageable).map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        UUID tenantId = TenantContext.require();
        User user = userRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new CustomException.NotFound("User not found: " + id));
        return userMapper.toResponse(user);
    }

    /**
     * Удаление по правилам:
     *  1. Нельзя удалить себя.
     *  2. Сидовые пользователи (createdBy = null) — неудаляемы.
     *  3. Созданные PLATFORM_ADMIN — неудаляемы из тенанта.
     *  4. Создатель может удалить своего пользователя (при условии, что роль удаляемого <= своей).
     *  5. Остальные — 403.
     */
    @Transactional
    public void delete(UUID id) {
        UUID tenantId = TenantContext.require();
        User user = userRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new CustomException.NotFound("User not found: " + id));

        UUID me = currentUserId();
        if (me != null && me.equals(user.getId())) {
            throw new CustomException.Forbidden("Cannot delete yourself");
        }
        if (user.getCreatedBy() == null) {
            throw new CustomException.Forbidden("System user cannot be deleted");
        }
        if (user.getCreatedByRole() == Role.PLATFORM_ADMIN) {
            throw new CustomException.Forbidden("Created by platform admin — cannot be deleted");
        }
        if (!user.getCreatedBy().equals(me)) {
            throw new CustomException.Forbidden("Only creator can delete this user");
        }

        // Дополнительно: роль удаляемого должна быть строго ниже текущей
        checkCanManageRole(user.getRole());

        userRepository.delete(user);
    }

    // ==========================================
    // Временные роли
    // ==========================================

    @Transactional
    public UserResponse assignTempRole(UUID userId, Role role, LocalDateTime validUntil) {
        UUID tenantId = TenantContext.require();

        checkCanAssignRole(role);

        if (validUntil != null && validUntil.isBefore(LocalDateTime.now())) {
            throw new CustomException.BadRequest("validUntil must be in the future");
        }

        User user = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new CustomException.NotFound("User not found: " + userId));

        boolean alreadyHasActive = user.getTempRoles().stream()
                .anyMatch(ur -> ur.getRole() == role && ur.isActiveNow());
        if (alreadyHasActive) {
            throw new CustomException.Conflict("User already has active temp role " + role);
        }

        UserRole tempRole = UserRole.builder()
                .user(user)
                .role(role)
                .validFrom(LocalDateTime.now())
                .validUntil(validUntil)
                .build();

        userRoleRepository.save(tempRole);
        user.getTempRoles().add(tempRole);

        return userMapper.toResponse(user);
    }

    @Transactional
    public void revokeTempRole(UUID userId, Role role) {
        UUID tenantId = TenantContext.require();

        checkCanManageRole(role);

        User user = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new CustomException.NotFound("User not found: " + userId));

        userRoleRepository.deleteByUserIdAndRole(userId, role);
        user.getTempRoles().removeIf(ur -> ur.getRole() == role);
    }
}