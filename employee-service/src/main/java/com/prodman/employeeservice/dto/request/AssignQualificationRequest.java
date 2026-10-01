package com.prodman.employeeservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignQualificationRequest {

    @NotNull
    private UUID qualificationId;

    @Min(1)
    private Integer level;

    private UUID assignedBy;

    @Size(max = 2000)
    private String notes;
}