package com.prodman.userservice.dto.response;

import com.prodman.userservice.model.TenantPlan;
import com.prodman.userservice.model.TenantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse {
    private UUID id;
    private String name;
    private String slug;
    private TenantStatus status;
    private TenantPlan plan;
    private LocalDateTime trialEndsAt;
    private LocalDateTime subscriptionEndsAt;
    private LocalDateTime createdAt;
}