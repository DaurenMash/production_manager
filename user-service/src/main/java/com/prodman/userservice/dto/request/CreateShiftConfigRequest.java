package com.prodman.userservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShiftConfigRequest {
    private String name;
    private Integer shiftCount;
    private List<ShiftDto> shifts;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShiftDto {
        private String name;
        private String startTime;
        private String endTime;
        private Integer displayOrder;
        private String color;
    }
}