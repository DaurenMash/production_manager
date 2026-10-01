package com.prodman.employeeservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeRequest {

    @NotBlank
    @Size(max = 64)
    private String code;

    @NotBlank
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    @Size(max = 100)
    private String middleName;

    @NotBlank
    @Pattern(regexp = "\\d{10}", message = "Телефон: 10 цифр без +7")
    private String phone;

    private LocalDate hiredAt;
    private LocalDate firedAt;

    private UUID departmentId;
    private UUID positionId;
    private UUID userId;

    private Integer maxConsecutiveHours;
}