package com.prodman.userservice.controller;

import com.prodman.userservice.dto.request.LoginRequest;
import com.prodman.userservice.dto.request.RefreshTokenRequest;
import com.prodman.userservice.dto.request.RegisterTenantRequest;
import com.prodman.userservice.dto.response.AuthResponse;
import com.prodman.userservice.dto.response.UserResponse;
import com.prodman.userservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register-tenant")
    public ResponseEntity<UserResponse> registerTenant(@Valid @RequestBody RegisterTenantRequest req) {
        return ResponseEntity.ok(authService.registerTenant(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        return ResponseEntity.ok(authService.refresh(req));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // JWT stateless — logout фактически на клиенте (удалить токен).
        // Позже можно добавить blacklist через Redis.
        return ResponseEntity.noContent().build();
    }
}