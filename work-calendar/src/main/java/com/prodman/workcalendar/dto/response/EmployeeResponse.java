package com.prodman.workcalendar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private List<String> positions;
    private String status;
    private String statusDisplay;
    private String phoneNumber;
    private String department;
    private Integer maxHoursPerWeek;
    private String preferredShift;
    private LocalDate vacationStart;
    private LocalDate vacationEnd;
    private LocalDate sickLeaveStart;
    private LocalDate sickLeaveEnd;
    private boolean isAvailable;
}