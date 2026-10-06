package com.prodman.workstation.dto.response;

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
public class WorkstationResponse {
    private UUID id;
    private UUID departmentId;
    private UUID requiredQualificationId;
    private String code;
    private String name;
    private String description;
    private Boolean isActive;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}