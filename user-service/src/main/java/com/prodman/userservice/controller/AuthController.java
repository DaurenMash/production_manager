package com.prodman.userservice.controller;

import com.prodman.userservice.dto.request.LoginRequest;
import com.prodman.userservice.dto.request.RefreshTokenRequest;
import com.prodman.userservice.dto.request.RegisterRequest;
import com.prodman.userservice.dto.response.AuthResponse;
import com.prodman.userservice.dto.response.UserResponse;
import com.prodman.userservice.model.Role;
import com.prodman.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API for authentication and authorization")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and return JWT tokens")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = AuthResponse.builder()
                .accessToken("fake-token-for-development")
                .refreshToken("fake-refresh-token")
                .userId("admin-id")
                .username(request.getUsername())
                .email("admin@prodman.com")
                .role(Role.ADMIN)
                .expiresIn(86400000L)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-password")
    public ResponseEntity<?> testPassword(@RequestParam String raw) {
        return ResponseEntity.ok(passwordEncoder.encode(raw));
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = AuthResponse.builder()
                .accessToken("fake-refreshed-token")
                .refreshToken("fake-refresh-token")
                .userId("admin-id")
                .username("admin")
                .email("admin@prodman.com")
                .role(Role.ADMIN)
                .expiresIn(86400000L)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Invalidate current JWT token")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        System.out.println("Logout endpoint called");
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get info of currently authenticated user")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestParam String username) {
        UserResponse response = UserResponse.builder()
                .id("admin-id")
                .username("admin")
                .email("admin@prodman.com")
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        return ResponseEntity.ok(response);
    }
}

















//package com.prodman.userservice.controller;
//
//
//
//
//
//import com.prodman.userservice.dto.response.AuthResponse;
//import com.prodman.userservice.model.Role;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/v1/auth")
//public class AuthController {
//
//    @PostMapping("/login")
//    public ResponseEntity<AuthResponse> login() {
//        AuthResponse response = new AuthResponse();
//        response.setAccessToken("fake-token");
//        response.setRefreshToken("fake-refresh");
//        response.setUserId("admin-id");
//        response.setUsername("admin");
//        response.setEmail("admin@prodman.com");
//        response.setRole(Role.ADMIN);
//        response.setExpiresIn(86400000L);
//        return ResponseEntity.ok(response);
//    }
//}
