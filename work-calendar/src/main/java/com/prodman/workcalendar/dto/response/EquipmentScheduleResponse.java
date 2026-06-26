package com.prodman.workcalendar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentScheduleResponse {
    private String id;
    private String equipmentId;
    private String equipmentName;
    private LocalDate date;
    private boolean working;
    private String shiftType;
    private String shiftTypeDisplay;
    private String note;
    private boolean maintenanceScheduled;
}