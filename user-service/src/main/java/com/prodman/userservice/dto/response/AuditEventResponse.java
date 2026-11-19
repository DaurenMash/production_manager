package com.prodman.userservice.dto.response;

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
public class AuditEventResponse {
    private UUID id;
    private UUID tenantId;
    private UUID userId;
    private String username;
    private String action;
    private String target;
    private String ip;
    private String metadata;
    private LocalDateTime createdAt;
}