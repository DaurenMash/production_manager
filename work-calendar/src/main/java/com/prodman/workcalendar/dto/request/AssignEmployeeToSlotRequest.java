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

    /** Если true — назначить, несмотря на отсутствие требуемой квалификации. */
    private Boolean forceQualificationOverride;

    /** Причина override (обязательна при forceQualificationOverride=true). */
    private String qualificationOverrideReason;

    @Size(max = 2000)
    private String overrideReason;

    @Size(max = 2000)
    private String comment;
}