package com.prodman.workcalendar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {

    private LocalDate from;
    private LocalDate to;
    private List<Row> employees;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Row {
        private UUID employeeId;
        private String employeeFullName;
        private UUID departmentId;
        private String employeeStatus;
        private List<Cell> days;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Cell {
        private LocalDate date;
        /** SHIFT / VACATION / SICK_LEAVE / UNAVAILABLE / FIRED / DAY_OFF / EMPTY */
        private String kind;
        private String shiftCode;
        private String shiftName;
        private String color;
        private Boolean isDayOffWork;
        private BigDecimal totalHours;
        private BigDecimal nightHours;
        private UUID workstationId;
        private String workstationName;
    }
}