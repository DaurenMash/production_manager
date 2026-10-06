package com.prodman.workcalendar.dto.request;

import jakarta.validation.constraints.NotNull;
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
public class CreateShiftSlotRequest {

    @NotNull
    private LocalDate date;

    @NotNull
    private UUID workstationId;

    @NotNull
    private UUID shiftPatternId;

    /** Квалификация слота. NULL — берётся из workstation.required_qualification_id. */
    private UUID requiredQualificationId;
}