package com.prodman.userservice.controller;

import com.prodman.userservice.dto.request.RegisterRequest;
import com.prodman.userservice.dto.response.UserResponse;
import com.prodman.userservice.model.Role;
import com.prodman.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(userService.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(userService.register(req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // Временные роли
    // ==========================================

    @PostMapping("/{id}/temp-roles")
    public ResponseEntity<UserResponse> assignTempRole(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        Role role = Role.valueOf(body.get("role"));
        LocalDateTime validUntil = body.containsKey("validUntil")
                ? LocalDateTime.parse(body.get("validUntil"))
                : null;
        return ResponseEntity.ok(userService.assignTempRole(id, role, validUntil));
    }

    @DeleteMapping("/{id}/temp-roles/{role}")
    public ResponseEntity<Void> revokeTempRole(
            @PathVariable UUID id,
            @PathVariable Role role) {
        userService.revokeTempRole(id, role);
        return ResponseEntity.noContent().build();
    }
}