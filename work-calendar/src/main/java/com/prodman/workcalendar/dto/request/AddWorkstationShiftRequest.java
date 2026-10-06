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
public class AddWorkstationShiftRequest {

    @NotNull
    private UUID workstationId;

    @NotNull
    private LocalDate date;

    @NotNull
    private UUID shiftPatternId;

    /** Если TRUE — создаётся N слотов (OPEN). По умолчанию 0. */
    private Integer initialSlots;
}