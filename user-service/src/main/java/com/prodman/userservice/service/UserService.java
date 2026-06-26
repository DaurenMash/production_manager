package com.prodman.userservice.service;

import com.prodman.userservice.dto.request.RegisterRequest;
import com.prodman.userservice.dto.response.UserResponse;
import com.prodman.userservice.exception.CustomException;
import com.prodman.userservice.mapper.UserMapper;
import com.prodman.userservice.model.Role;
import com.prodman.userservice.model.User;
import com.prodman.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserResponse register(RegisterRequest request) {
        System.out.println("DEBUG: Register request - username=" + request.getUsername() +
                ", email=" + request.getEmail() +
                ", role=" + request.getRole());

        if (userRepository.existsByUsername(request.getUsername())) {
            System.out.println("DEBUG: Username already exists: " + request.getUsername());
            throw new CustomException.Conflict("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            System.out.println("DEBUG: Email already exists: " + request.getEmail());
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
        System.out.println("DEBUG: User saved successfully - id=" + savedUser.getId());

        return userMapper.toResponse(savedUser);
    }

    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException.NotFound("User not found with id: " + id));
        return userMapper.toResponse(user);
    }

    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException.NotFound("User not found with username: " + username));
        return userMapper.toResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException.NotFound("User not found: " + username));
    }

    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new CustomException.NotFound("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}