package com.prodman.employeeservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeRequest {
    @NotBlank(message = "Имя обязательно")
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    private String lastName;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Неверный формат email")
    private String email;

    private String phoneNumber;
    private String department;
    private String position;

    private String userId; // опционально — если сотрудник является пользователем
}