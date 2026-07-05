package com.prodman.workstation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkstationRequest {
    @NotBlank(message = "Отдел обязателен")
    private String department;

    @NotBlank(message = "Название обязательно")
    private String title;

    @NotBlank(message = "Email создателя обязателен")
    private String createdBy;

    private List<String> employeeIds; // ID сотрудников
}