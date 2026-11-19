package com.prodman.userservice.service;

import com.prodman.userservice.dto.response.AuthResponse;
import com.prodman.userservice.dto.response.TenantResponse;
import com.prodman.userservice.exception.CustomException;
import com.prodman.userservice.model.Role;
import com.prodman.userservice.model.Tenant;
import com.prodman.userservice.model.User;
import com.prodman.userservice.repository.TenantRepository;
import com.prodman.userservice.repository.UserRepository;
import com.prodman.userservice.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Page<TenantResponse> listAll(Pageable pageable) {
        return tenantRepository.findAll(pageable)
                .map(t -> TenantResponse.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .slug(t.getSlug())
                        .status(t.getStatus())
                        .plan(t.getPlan())
                        .trialEndsAt(t.getTrialEndsAt())
                        .subscriptionEndsAt(t.getSubscriptionEndsAt())
                        .createdAt(t.getCreatedAt())
                        .build()
                );
    }

    @Transactional(readOnly = true)
    public AuthResponse impersonate(UUID tenantId) {
        UUID platformUserId = JwtAuthenticationFilter.currentUserId();
        if (platformUserId == null) {
            throw new CustomException.Forbidden("No authenticated user");
        }

        User platformAdmin = userRepository.findById(platformUserId)
                .orElseThrow(() -> new CustomException.NotFound(
                        "Platform admin not found: " + platformUserId));

        if (platformAdmin.getRole() != Role.PLATFORM_ADMIN) {
            throw new CustomException.Forbidden("Only PLATFORM_ADMIN can impersonate");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new CustomException.NotFound("Tenant not found: " + tenantId));

        List<String> roles = List.of(Role.ADMIN.name());

        String accessToken = jwtService.generateToken(
                platformAdmin.getUsername(),
                platformAdmin.getId(),
                roles,
                tenant.getId(),
                tenant.getStatus().name(),
                null
        );

        String refreshToken = jwtService.generateRefreshToken(
                platformAdmin.getUsername(),
                platformAdmin.getId(),
                tenant.getId()
        );

        auditService.record(
                tenant.getId(),
                platformAdmin.getId(),
                platformAdmin.getUsername(),
                "IMPERSONATE",
                "tenant:" + tenant.getSlug(),
                clientIp(),
                null
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(platformAdmin.getId())
                .username(platformAdmin.getUsername())
                .email(platformAdmin.getEmail())
                .role(Role.ADMIN)
                .tenantId(tenant.getId())
                .tenantSlug(tenant.getSlug())
                .tenantStatus(tenant.getStatus())
                .tenantPlan(tenant.getPlan())
                .departmentId(null)
                .expiresIn(86400000L)
                .build();
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