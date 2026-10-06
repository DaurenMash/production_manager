package com.prodman.workcalendar.dto.request;

import jakarta.validation.constraints.NotNull;
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
public class SetWorkstationDayStatusRequest {

    @NotNull
    private UUID workstationId;

    @NotNull
    private LocalDate date;

    @NotNull
    private Boolean isWorking;

    @Size(max = 2000)
    private String note;
}