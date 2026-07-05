package com.prodman.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftConfigResponse {
    private String id;
    private String name;
    private Integer shiftCount;
    private Boolean isActive;
    private List<ShiftDto> shifts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShiftDto {
        private String id;
        private String name;
        private String startTime;
        private String endTime;
        private Integer displayOrder;
        private String color;
    }
}