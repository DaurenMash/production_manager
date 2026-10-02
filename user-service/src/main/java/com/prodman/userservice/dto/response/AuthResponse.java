package com.prodman.userservice.dto.response;

import com.prodman.userservice.model.Role;
import com.prodman.userservice.model.TenantPlan;
import com.prodman.userservice.model.TenantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private UUID userId;
    private String username;
    private String email;
    private Role role;
    /** NULL для PLATFORM_ADMIN. */
    private UUID tenantId;
    private String tenantSlug;
    private TenantStatus tenantStatus;
    private TenantPlan tenantPlan;
    private long expiresIn;
}