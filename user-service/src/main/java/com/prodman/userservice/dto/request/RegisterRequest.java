// DTO RegisterRequest больше не используется.
// Регистрация нового тенанта — RegisterTenantRequest.
// Добавление пользователя в существующий тенант — через AdminUserController (позже).
package com.prodman.userservice.dto.request;

import com.prodman.userservice.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Регистрация пользователя внутри существующего тенанта.
 * Используется администратором тенанта (не публичный эндпоинт).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank
    @Size(min = 3, max = 64)
    private String username;

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    @Builder.Default
    private Role role = Role.VISITOR;
}