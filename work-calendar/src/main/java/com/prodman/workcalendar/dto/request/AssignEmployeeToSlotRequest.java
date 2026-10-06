package com.prodman.workcalendar.dto.request;

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
public class AssignEmployeeToSlotRequest {

    @NotNull
    private UUID employeeId;

    private Boolean force;

    @Size(max = 2000)
    private String overrideReason;

    @Size(max = 2000)
    private String comment;
}