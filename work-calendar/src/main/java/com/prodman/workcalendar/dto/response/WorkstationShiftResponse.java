package com.prodman.workcalendar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkstationShiftResponse {
    private UUID id;
    private UUID workstationId;
    private LocalDate date;
    private UUID shiftPatternId;
    private String shiftPatternName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}