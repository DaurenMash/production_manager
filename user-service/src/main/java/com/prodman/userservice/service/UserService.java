package com.prodman.userservice.service;

import com.prodman.userservice.dto.request.RegisterRequest;
import com.prodman.userservice.dto.response.UserResponse;
import com.prodman.userservice.exception.CustomException;
import com.prodman.userservice.mapper.UserMapper;
import com.prodman.userservice.model.Role;
import com.prodman.userservice.model.User;
import com.prodman.userservice.repository.UserRepository;
import com.prodman.userservice.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    /**
     * Регистрация пользователя внутри текущего тенанта.
     * Вызывается администратором тенанта.
     */
    @Transactional
    public UserResponse register(RegisterRequest req) {
        UUID tenantId = TenantContext.require();

        if (userRepository.existsByTenantIdAndUsername(tenantId, req.getUsername())) {
            throw new CustomException.Conflict("Username already exists");
        }
        if (userRepository.existsByTenantIdAndEmail(tenantId, req.getEmail())) {
            throw new CustomException.Conflict("Email already exists");
        }

        User user = User.builder()
                .tenantId(tenantId)
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole() != null ? req.getRole() : Role.VISITOR)
                .enabled(true)
                .build();
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> list(Pageable pageable) {
        UUID tenantId = TenantContext.require();
        return userRepository.findAllByTenantId(tenantId, pageable).map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        UUID tenantId = TenantContext.require();
        User user = userRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new CustomException.NotFound("User not found: " + id));
        return userMapper.toResponse(user);
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = TenantContext.require();
        User user = userRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new CustomException.NotFound("User not found: " + id));
        userRepository.delete(user);
    }
}