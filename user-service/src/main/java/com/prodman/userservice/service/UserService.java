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
    private final AuditService auditService;

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

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName();
    }

    private UUID currentUserId() {
        return JwtAuthenticationFilter.currentUserId();
    }

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

    @Transactional
    public UserResponse register(RegisterRequest req) {
        UUID tenantId = TenantContext.require();
        Role role = req.getRole() != null ? req.getRole() : Role.TENANT_VISITOR;

        checkCanAssignRole(role);

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

        User saved = userRepository.save(user);

        auditService.record(
                tenantId,
                currentUserId(),
                currentUsername(),
                "CREATE_USER",
                "user:" + saved.getUsername(),
                clientIp(),
                "{\"role\":\"" + role.name() + "\"}"
        );

        return userMapper.toResponse(saved);
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

        checkCanManageRole(user.getRole());

        auditService.record(
                tenantId,
                me,
                currentUsername(),
                "DELETE_USER",
                "user:" + user.getUsername(),
                clientIp(),
                null
        );

        userRepository.delete(user);
    }

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

        auditService.record(
                tenantId,
                currentUserId(),
                currentUsername(),
                "ASSIGN_TEMP_ROLE",
                "user:" + user.getUsername(),
                clientIp(),
                "{\"role\":\"" + role.name() + "\",\"validUntil\":"
                        + (validUntil != null ? "\"" + validUntil + "\"" : "null") + "}"
        );

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

        auditService.record(
                tenantId,
                currentUserId(),
                currentUsername(),
                "REVOKE_TEMP_ROLE",
                "user:" + user.getUsername(),
                clientIp(),
                "{\"role\":\"" + role.name() + "\"}"
        );
    }

    private String clientIp() {
        var attrs = (org.springframework.web.context.request.ServletRequestAttributes)
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;
        var req = attrs.getRequest();
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return req.getRemoteAddr();
    }
}