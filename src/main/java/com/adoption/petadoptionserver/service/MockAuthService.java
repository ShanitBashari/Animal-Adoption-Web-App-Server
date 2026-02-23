package com.adoption.petadoptionserver.service;

import com.adoption.petadoptionserver.dto.*;
import com.adoption.petadoptionserver.interfaces.AuthService;
import com.adoption.petadoptionserver.interfaces.UserService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.UUID;

@Service
@Profile("mock")
public class MockAuthService implements AuthService {

    private final UserService userService;

    public MockAuthService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDto register(RegisterRequest request) {
        // naive register: check username taken
        if (userService.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("username taken");
        }
        UserDto dto = new UserDto();
        dto.setUsername(request.getUsername());
        dto.setEmail(request.getEmail());
        dto.setFullName(request.getFullName());
        dto.setPhone(request.getPhone());
        return userService.create(dto);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        var userOpt = userService.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) throw new IllegalArgumentException("invalid credentials");
        var user = userOpt.get();
        // In mock: accept any password
        String token = Base64.getEncoder().encodeToString((user.getUsername() + ":" + UUID.randomUUID()).getBytes());
        return AuthResponse.builder()
                .accessToken(token)
                .refreshToken(UUID.randomUUID().toString())
                .userId(user.getId())
                .username(user.getUsername())
                .roles(user.getRoles())
                .expiresIn(3600L)
                .build();
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        // naive refresh: return new token
        return AuthResponse.builder()
                .accessToken(Base64.getEncoder().encodeToString(("refreshed:" + refreshToken).getBytes()))
                .refreshToken(UUID.randomUUID().toString())
                .expiresIn(3600L)
                .build();
    }

    @Override
    public void logout(String token) {
        // no-op for mock
    }
}