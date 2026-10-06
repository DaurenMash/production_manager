package com.prodman.workcalendar.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShiftPatternRequest {

    @NotBlank
    @Size(max = 64)
    private String code;

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    private Boolean crossesMidnight;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal totalHours;

    @DecimalMin("0.00")
    private BigDecimal nightHours;

    private LocalTime nightWindowStart;
    private LocalTime nightWindowEnd;

    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal coefficient;

    private Boolean isActive;
}