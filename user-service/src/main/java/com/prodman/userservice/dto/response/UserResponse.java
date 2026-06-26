package com.prodman.userservice.dto.response;

import com.prodman.userservice.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private Role role;  // может быть ADMIN, OPERATOR, ANALYST, VISITOR
    private boolean enabled;
    private LocalDateTime createdAt;
}