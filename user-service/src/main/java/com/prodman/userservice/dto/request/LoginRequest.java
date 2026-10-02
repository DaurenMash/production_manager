package com.prodman.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * Slug тенанта. Обязателен для обычного пользователя.
     * Для PLATFORM_ADMIN (суперадмин платформы) — не нужен (null).
     */
    private String slug;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}