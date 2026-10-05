package com.prodman.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private Integer shiftCount;

    @Builder.Default
    private Boolean isActive = false;

    @NotEmpty
    private List<ShiftDto> shifts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShiftDto {
        @NotBlank
        private String name;

        @NotBlank
        private String startTime;

        @NotBlank
        private String endTime;

        @NotNull
        private Integer displayOrder;

        @NotBlank
        private String color;
    }
}