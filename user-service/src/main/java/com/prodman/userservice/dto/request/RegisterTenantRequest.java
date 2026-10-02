package com.prodman.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterTenantRequest {

    @NotBlank
    @Size(min = 2, max = 255)
    private String tenantName;

    @NotBlank
    @Size(min = 2, max = 64)
    @Pattern(regexp = "^[a-z0-9][a-z0-9-]*[a-z0-9]$",
             message = "Slug: строчные латинские буквы, цифры и дефис; не начинается и не заканчивается дефисом")
    private String tenantSlug;

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
}