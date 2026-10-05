package com.prodman.userservice.service;

import com.prodman.userservice.dto.request.LoginRequest;
import com.prodman.userservice.dto.request.RefreshTokenRequest;
import com.prodman.userservice.dto.request.RegisterTenantRequest;
import com.prodman.userservice.dto.response.AuthResponse;
import com.prodman.userservice.dto.response.UserResponse;
import com.prodman.userservice.exception.CustomException;
import com.prodman.userservice.mapper.UserMapper;
import com.prodman.userservice.model.*;
import com.prodman.userservice.repository.TenantRepository;
import com.prodman.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int DEFAULT_TRIAL_DAYS = 30;

    private final JwtService jwtService;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    /**
     * Регистрация нового тенанта (клиента) и первого пользователя-администратора.
     * По умолчанию: status=TRIAL, plan=FREE, trial_ends_at = now + 30 дней.
     */
    @Transactional
    public UserResponse registerTenant(RegisterTenantRequest req) {
        if (tenantRepository.existsBySlug(req.getTenantSlug())) {
            throw new CustomException.Conflict("Tenant slug already exists: " + req.getTenantSlug());
        }

        Tenant tenant = Tenant.builder()
                .name(req.getTenantName())
                .slug(req.getTenantSlug())
                .status(TenantStatus.TRIAL)
                .plan(TenantPlan.FREE)
                .trialEndsAt(LocalDateTime.now().plusDays(DEFAULT_TRIAL_DAYS))
                .build();
        tenant = tenantRepository.save(tenant);

        if (userRepository.existsByTenantIdAndUsername(tenant.getId(), req.getUsername())) {
            throw new CustomException.Conflict("Username already exists in tenant");
        }
        if (userRepository.existsByTenantIdAndEmail(tenant.getId(), req.getEmail())) {
            throw new CustomException.Conflict("Email already exists in tenant");
        }

        User user = User.builder()
                .tenantId(tenant.getId())
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        user = userRepository.save(user);

        return userMapper.toResponse(user);
    }

    /**
     * Логин.
     *  - Если slug не указан — ищем PLATFORM_ADMIN (tenant_id = null).
     *  - Если slug указан — находим тенант, потом юзера по (tenantId, username).
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user;
        Tenant tenant = null;

        if (req.getSlug() == null || req.getSlug().isBlank()) {
            // платформенный админ
            user = userRepository.findByTenantIdIsNullAndUsername(req.getUsername())
                    .orElseThrow(() -> new CustomException.Unauthorized("Invalid credentials"));
        } else {
            tenant = tenantRepository.findBySlug(req.getSlug())
                    .orElseThrow(() -> new CustomException.Unauthorized("Invalid credentials"));
            user = userRepository.findByTenantIdAndUsername(tenant.getId(), req.getUsername())
                    .orElseThrow(() -> new CustomException.Unauthorized("Invalid credentials"));
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new CustomException.Unauthorized("Invalid credentials");
        }
        if (!user.isEnabled()) {
            throw new CustomException.Unauthorized("User disabled");
        }
        if (tenant != null && (tenant.getStatus() == TenantStatus.SUSPENDED
                || tenant.getStatus() == TenantStatus.CANCELLED)) {
            throw new CustomException.Unauthorized("Tenant is " + tenant.getStatus());
        }

        String role = "ROLE_" + user.getRole().name();
        String tenantStatus = tenant == null ? null : tenant.getStatus().name();

        String accessToken = jwtService.generateToken(
                user.getUsername(), user.getId(), role, user.getTenantId(), tenantStatus);
        String refreshToken = jwtService.generateRefreshToken(
                user.getUsername(), user.getId(), user.getTenantId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .tenantId(user.getTenantId())
                .tenantSlug(tenant == null ? null : tenant.getSlug())
                .tenantStatus(tenant == null ? null : tenant.getStatus())
                .tenantPlan(tenant == null ? null : tenant.getPlan())
                .expiresIn(86400000L)
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshTokenRequest req) {
        String token = req.getRefreshToken();
        if (!jwtService.isTokenValid(token)) {
            throw new CustomException.Unauthorized("Invalid or expired refresh token");
        }
        String username = jwtService.extractUsername(token);
        UUID tenantId = jwtService.extractTenantId(token);

        User user;
        Tenant tenant = null;
        if (tenantId == null) {
            user = userRepository.findByTenantIdIsNullAndUsername(username)
                    .orElseThrow(() -> new CustomException.NotFound("User not found"));
        } else {
            tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(() -> new CustomException.NotFound("Tenant not found"));
            user = userRepository.findByTenantIdAndUsername(tenantId, username)
                    .orElseThrow(() -> new CustomException.NotFound("User not found"));
        }

        String role = "ROLE_" + user.getRole().name();
        String tenantStatus = tenant == null ? null : tenant.getStatus().name();

        String newAccess = jwtService.generateToken(
                user.getUsername(), user.getId(), role, user.getTenantId(), tenantStatus);
        String newRefresh = jwtService.generateRefreshToken(
                user.getUsername(), user.getId(), user.getTenantId());

        return AuthResponse.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .tenantId(user.getTenantId())
                .tenantSlug(tenant == null ? null : tenant.getSlug())
                .tenantStatus(tenant == null ? null : tenant.getStatus())
                .tenantPlan(tenant == null ? null : tenant.getPlan())
                .expiresIn(86400000L)
                .build();
    }
}