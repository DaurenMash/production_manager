package com.prodman.userservice.dto.response;

import com.prodman.userservice.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private UUID tenantId;
    private String username;
    private String email;
    private Role role;
    private boolean enabled;
    private LocalDateTime createdAt;
}