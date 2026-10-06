package com.prodman.workcalendar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayBoardResponse {
    private LocalDate date;
    private List<WorkstationBoard> workstations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkstationBoard {
        private UUID workstationId;
        private String workstationName;
        private UUID departmentId;
        private UUID requiredQualificationId;
        private Boolean isWorking;
        private List<ShiftBoard> shifts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShiftBoard {
        private UUID workstationShiftId;
        private UUID shiftPatternId;
        private String shiftPatternName;
        private java.math.BigDecimal totalHours;
        private java.math.BigDecimal nightHours;
        private List<ShiftSlotResponse> slots;
    }
}