package com.prodman.employeeservice.dto.response;

import com.prodman.employeeservice.model.EmployeeStatus;
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
public class EmployeeResponse {
    private UUID id;
    private String code;
    private String firstName;
    private String lastName;
    private String middleName;
    private String phone;
    private LocalDate hiredAt;
    private LocalDate firedAt;
    private EmployeeStatus status;
    private UUID departmentId;
    private String departmentName;
    private UUID positionId;
    private String positionName;
    private UUID userId;
    private Integer maxConsecutiveHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}