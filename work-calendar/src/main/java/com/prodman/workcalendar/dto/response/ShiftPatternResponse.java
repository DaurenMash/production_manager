package com.prodman.workcalendar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftPatternResponse {
    private UUID id;
    private String code;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean crossesMidnight;
    private BigDecimal totalHours;
    private BigDecimal nightHours;
    private LocalTime nightWindowStart;
    private LocalTime nightWindowEnd;
    private BigDecimal coefficient;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}