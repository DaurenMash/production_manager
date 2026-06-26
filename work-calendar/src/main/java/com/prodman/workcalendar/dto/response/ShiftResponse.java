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
public class ShiftResponse {
    private String id;
    private String employeeId;
    private String employeeName;
    private LocalDate date;
    private String shiftType;
    private boolean available;
    private String status;
}