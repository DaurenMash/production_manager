package com.prodman.employeeservice.dto.request;

import com.prodman.employeeservice.model.EmployeeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeRequest {

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String departmentId;
    private String positionId;
    private EmployeeStatus status;
    private Integer maxConsecutiveHours;
    private String userId;
}