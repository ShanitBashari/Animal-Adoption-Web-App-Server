package com.adoption.petadoptionserver.controller;

import com.adoption.petadoptionserver.dto.*;
import com.adoption.petadoptionserver.interfaces.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Register new user -> returns 201 Created + Location header
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request, UriComponentsBuilder uriBuilder) {
        UserDto created = authService.register(request);
        URI uri = uriBuilder.path("/api/users/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(uri).body(created);
    }

    // Login - return token / auth response
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse auth = authService.login(loginRequest);
        return ResponseEntity.ok(auth);
    }
}