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
public class UserTempRoleResponse {
    private UUID id;
    private Role role;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
}