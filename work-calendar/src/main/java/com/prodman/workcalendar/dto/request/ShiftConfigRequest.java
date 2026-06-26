package com.prodman.workcalendar.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftConfigRequest {
    @NotBlank(message = "Shift config is required")
    private String shiftConfig; // "2_SHIFTS_12H" или "3_SHIFTS_8H"

    private String notes;
}