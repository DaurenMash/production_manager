package com.prodman.userservice.mapper;

import com.prodman.userservice.dto.response.UserResponse;
import com.prodman.userservice.dto.response.UserTempRoleResponse;
import com.prodman.userservice.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId())
                .tenantId(user.getTenantId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .departmentId(user.getDepartmentId())
                .tempRoles(mapTempRoles(user))
                .createdBy(user.getCreatedBy())
                .createdByRole(user.getCreatedByRole())
                .build();
    }

    private List<UserTempRoleResponse> mapTempRoles(User user) {
        if (user.getTempRoles() == null || user.getTempRoles().isEmpty()) {
            return List.of();
        }
        return user.getTempRoles().stream()
                .map(tr -> UserTempRoleResponse.builder()
                        .id(tr.getId())
                        .role(tr.getRole())
                        .validFrom(tr.getValidFrom())
                        .validUntil(tr.getValidUntil())
                        .build())
                .toList();
    }
}