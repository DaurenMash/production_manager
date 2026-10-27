package com.prodman.employeeservice.dto.request;

import com.prodman.employeeservice.model.MonthCrossingMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateTenantSettingsRequest {

    @NotNull
    private MonthCrossingMode monthCrossingMode;

    @NotNull
    @Size(max = 64)
    private String baseCoefficientName;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal baseCoefficientValue;

    @NotNull
    @Size(max = 64)
    private String weekendCoefficientName;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal weekendCoefficientValue;

    @NotNull
    @Size(max = 64)
    private String nightCoefficientName;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal nightCoefficientValue;
}