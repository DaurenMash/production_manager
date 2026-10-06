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
public class WorkstationDayStatusResponse {
    private UUID id;
    private UUID workstationId;
    private LocalDate date;
    private Boolean isWorking;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}