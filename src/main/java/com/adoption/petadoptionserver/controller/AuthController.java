package com.adoption.petadoptionserver.controller;

import com.adoption.petadoptionserver.dto.AuthResponse;
import com.adoption.petadoptionserver.dto.LoginRequest;
import com.adoption.petadoptionserver.dto.RegisterRequest;
import com.adoption.petadoptionserver.dto.UserDto;
import com.adoption.petadoptionserver.interfaces.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Controller responsible for authentication operations,
 * such as user registration and login.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user.
     *
     * @param request the registration request
     * @param uriBuilder URI builder used to create the Location header
     * @return the created user with HTTP 201 status
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(
            @Valid @RequestBody RegisterRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        UserDto created = authService.register(request);
        URI location = uriBuilder.path("/api/users/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    /**
     * Authenticates a user and returns authentication data.
     *
     * @param loginRequest the login request containing credentials
     * @return authentication response including tokens or user details
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity.ok(authResponse);
    }
}