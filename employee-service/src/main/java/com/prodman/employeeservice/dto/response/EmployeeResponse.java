package com.prodman.employeeservice.dto.response;

import com.prodman.employeeservice.model.EmployeeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    private PositionResponse position;
    private DepartmentResponse department;

    private EmployeeStatus status;
    private Integer maxConsecutiveHours;
    private String userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}