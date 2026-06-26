package com.prodman.userservice.service;

import com.prodman.userservice.dto.request.LoginRequest;
import com.prodman.userservice.dto.request.RefreshTokenRequest;
import com.prodman.userservice.dto.request.RegisterRequest;
import com.prodman.userservice.dto.response.AuthResponse;
import com.prodman.userservice.dto.response.UserResponse;
import com.prodman.userservice.exception.CustomException;
import com.prodman.userservice.mapper.UserMapper;
import com.prodman.userservice.model.Role;
import com.prodman.userservice.model.User;
import com.prodman.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException.Conflict("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException.Conflict("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.VISITOR)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        // ВРЕМЕННОЕ РЕШЕНИЕ: всегда возвращаем токен без проверки
        return AuthResponse.builder()
                .accessToken("fake-token-for-development")
                .refreshToken("fake-refresh-token")
                .userId("admin-id")
                .username(request.getUsername() != null ? request.getUsername() : "admin")
                .email("admin@prodman.com")
                .role(Role.ADMIN)
                .expiresIn(86400000L)
                .build();
    }

//    public AuthResponse login(LoginRequest request) {
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
//        );
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//        User user = userRepository.findByUsername(userDetails.getUsername())
//                .orElseThrow(() -> new CustomException.NotFound("User not found"));
//
//        String accessToken = jwtService.generateToken(userDetails);
//        String refreshToken = jwtService.generateRefreshToken(userDetails);
//
//        return AuthResponse.builder()
//                .accessToken(accessToken)
//                .refreshToken(refreshToken)
//                .userId(user.getId())
//                .username(user.getUsername())
//                .email(user.getEmail())
//                .role(user.getRole())
//                .expiresIn(86400000L)
//                .build();
//    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        String username = jwtService.extractUsername(refreshToken);

        if (username == null) {
            throw new CustomException.Unauthorized("Invalid refresh token");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException.NotFound("User not found"));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new CustomException.Unauthorized("Invalid or expired refresh token");
        }

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .expiresIn(86400000L)
                .build();
    }

    public void logout(String token) {
        System.out.println("Logout called with token: " + token);
    }

    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException.NotFound("User not found with username: " + username));
        return userMapper.toResponse(user);
    }
}