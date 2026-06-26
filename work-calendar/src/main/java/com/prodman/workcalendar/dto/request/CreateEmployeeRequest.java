package com.prodman.workcalendar.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateEmployeeRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private List<String> positions;

    private String status;

    private String phoneNumber;

    private String department;

    private Integer maxHoursPerWeek;

    private String preferredShift;

    private LocalDate vacationStart;

    private LocalDate vacationEnd;

    private LocalDate sickLeaveStart;

    private LocalDate sickLeaveEnd;
}