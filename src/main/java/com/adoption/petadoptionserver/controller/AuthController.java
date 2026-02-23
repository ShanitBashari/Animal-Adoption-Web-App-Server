package com.adoption.petadoptionserver.controller;

import com.adoption.petadoptionserver.dto.*;
import com.adoption.petadoptionserver.interfaces.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Register new user
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // service should validate and throw or return result
        var result = authService.register(request);
        // result could be user DTO or a message
        return ResponseEntity.ok(result);
    }

    // Login - return token / auth response
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        AuthResponse auth = authService.login(loginRequest);
        return ResponseEntity.ok(auth);
    }

    // Optional: refresh token endpoint
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("Authorization") String refreshToken) {
        AuthResponse updated = authService.refresh(refreshToken);
        return ResponseEntity.ok(updated);
    }

    // Optional: logout (invalidate refresh token) - depends on implementation
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }
}
