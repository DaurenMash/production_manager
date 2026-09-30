package com.prodman.employeeservice.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePositionRequest {

    private String name;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Цвет должен быть в формате #RRGGBB")
    private String color;
}