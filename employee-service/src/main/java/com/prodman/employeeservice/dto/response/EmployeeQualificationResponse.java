package com.prodman.employeeservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeQualificationResponse {
    private UUID id;
    private UUID employeeId;
    private UUID qualificationId;
    private String qualificationCode;
    private String qualificationName;
    private Integer level;
    private LocalDate assignedAt;
    private UUID assignedBy;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}