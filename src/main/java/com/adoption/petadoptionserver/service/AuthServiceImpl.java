package com.adoption.petadoptionserver.service;

import com.adoption.petadoptionserver.dto.AuthResponse;
import com.adoption.petadoptionserver.dto.LoginRequest;
import com.adoption.petadoptionserver.dto.RegisterRequest;
import com.adoption.petadoptionserver.dto.UserDto;
import com.adoption.petadoptionserver.enums.UserRole;
import com.adoption.petadoptionserver.interfaces.AuthService;
import com.adoption.petadoptionserver.model.User;
import com.adoption.petadoptionserver.repository.UserRepository;
import com.adoption.petadoptionserver.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Service implementation responsible for user registration and authentication.
 */
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Converts a User entity into a UserDto.
     *
     * @param user the user entity
     * @return mapped user DTO
     */
    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRoles(List.of(user.getRole()));
        dto.setEnabled(user.getEnabled());
        dto.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        return dto;
    }

    /**
     * Registers a new user after validating request data and uniqueness constraints.
     *
     * @param request the registration request
     * @return created user DTO
     */
    @Override
    public UserDto register(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("username is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("password is required");
        }

        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();

        log.info("Registering new user with username={}", username);

        if (userRepository.existsByUsername(username)) {
            log.warn("Registration failed. Username already exists: {}", username);
            throw new IllegalStateException("username already exists");
        }

        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed. Email already exists: {}", email);
            throw new IllegalStateException("email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(String.valueOf(UserRole.USER));
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        log.info("User registered successfully with id={} username={}", savedUser.getId(), savedUser.getUsername());
        return toDto(savedUser);
    }

    /**
     * Authenticates a user and returns a JWT-based authentication response.
     *
     * @param request the login request
     * @return authentication response with access token and metadata
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("username is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("password is required");
        }

        String username = request.getUsername().trim();
        log.info("Login attempt for username={}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Login failed. User not found for username={}", username);
                    return new AuthenticationException("Invalid username or password");
                });

        if (Boolean.FALSE.equals(user.getEnabled())) {
            log.warn("Login failed. User is disabled. username={}", username);
            throw new AuthenticationException("User is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed. Invalid password for username={}", username);
            throw new AuthenticationException("Invalid username or password");
        }

        Instant now = Instant.now();
        long ttl = jwtService.getAccessTtlSeconds();
        Instant expiresAt = now.plusSeconds(ttl);

        String accessToken = jwtService.generateAccessToken(
                user.getUsername(),
                user.getId(),
                List.of(user.getRole())
        );

        log.info("Login successful for username={} userId={}", user.getUsername(), user.getId());

        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setExpiresIn(ttl);
        response.setIssuedAt(now);
        response.setExpiresAt(expiresAt);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRoles(List.of(user.getRole()));

        return response;
    }

    /**
     * Custom runtime exception used for authentication failures.
     */
    public static class AuthenticationException extends RuntimeException {

        public AuthenticationException(String message) {
            super(message);
        }
    }
}