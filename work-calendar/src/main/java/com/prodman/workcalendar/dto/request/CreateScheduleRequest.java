package com.prodman.workcalendar.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateScheduleRequest {
    @NotNull(message = "Date is required")
    private LocalDate date;

    private String shiftConfig;

    private String notes;
}