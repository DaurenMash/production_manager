package com.prodman.workcalendar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {
    private String id;
    private LocalDate date;
    private String shiftConfig;
    private String shiftConfigDisplay;
    private List<ShiftResponse> shifts;
    private String notes;
    private boolean hasConflicts;
    private List<String> conflicts;
}