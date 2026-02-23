package com.adoption.petadoptionserver.interfaces;

import com.adoption.petadoptionserver.dto.AuthResponse;
import com.adoption.petadoptionserver.dto.LoginRequest;
import com.adoption.petadoptionserver.dto.RegisterRequest;
import com.adoption.petadoptionserver.dto.UserDto;

public interface AuthService {
    UserDto register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(String refreshToken);
    void logout(String token);
}