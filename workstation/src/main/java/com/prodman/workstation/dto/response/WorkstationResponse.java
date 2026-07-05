package com.prodman.workstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkstationResponse {
    private String id;
    private String department;
    private String title;
    private String createdBy;
    private LocalDateTime createdAt;
    private Boolean isActive;
    private List<String> employeeIds;
    private List<EmployeeInfo> employees;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeInfo {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
    }
}