package com.prodman.employeeservice.dto.response;

import com.prodman.employeeservice.model.MonthCrossingMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantSettingsResponse {
    private UUID tenantId;
    private MonthCrossingMode monthCrossingMode;
    private String baseCoefficientName;
    private BigDecimal baseCoefficientValue;
    private String weekendCoefficientName;
    private BigDecimal weekendCoefficientValue;
    private String nightCoefficientName;
    private BigDecimal nightCoefficientValue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}