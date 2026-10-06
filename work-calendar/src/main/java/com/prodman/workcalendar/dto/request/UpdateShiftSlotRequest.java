package com.prodman.workcalendar.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShiftSlotRequest {

    private UUID requiredQualificationId;

    @DecimalMin("0.00")
    private BigDecimal actualHours;

    @DecimalMin("0.00")
    private BigDecimal actualNightHours;

    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal coefficientOverride;

    private String comment;
}