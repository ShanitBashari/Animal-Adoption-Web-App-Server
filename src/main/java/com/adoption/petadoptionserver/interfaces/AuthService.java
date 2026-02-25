package com.adoption.petadoptionserver.interfaces;

import com.adoption.petadoptionserver.dto.AuthResponse;
import com.adoption.petadoptionserver.dto.LoginRequest;
import com.adoption.petadoptionserver.dto.RegisterRequest;
import com.adoption.petadoptionserver.dto.UserDto;

public interface AuthService {
    /**
     * Register a new user.
     * Throws IllegalStateException if username/email already taken.
     * Throws IllegalArgumentException if input invalid.
     */
    UserDto register(RegisterRequest request);

    /**
     * Authenticate user and return tokens.
     * Throws AuthenticationException (runtime) on invalid credentials.
     */
    AuthResponse login(LoginRequest request);
}