package com.adoption.petadoptionserver.service;

import com.adoption.petadoptionserver.dto.AuthResponse;
import com.adoption.petadoptionserver.dto.LoginRequest;
import com.adoption.petadoptionserver.dto.RegisterRequest;
import com.adoption.petadoptionserver.dto.UserDto;
import com.adoption.petadoptionserver.interfaces.AuthService;
import com.adoption.petadoptionserver.model.User;
import com.adoption.petadoptionserver.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRoles(List.of(user.getRole()));
        dto.setEnabled(user.getEnabled());
        dto.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        // לא ממחזירים סיסמה
        return dto;
    }

    @Override
    public UserDto register(RegisterRequest request) {
        if (request == null) throw new IllegalArgumentException("request is required");
        if (request.getUsername() == null || request.getUsername().isBlank())
            throw new IllegalArgumentException("username is required");
        if (request.getEmail() == null || request.getEmail().isBlank())
            throw new IllegalArgumentException("email is required");
        if (request.getPassword() == null || request.getPassword().isBlank())
            throw new IllegalArgumentException("password is required");

        // uniqueness checks
        if (userRepo.existsByUsername(request.getUsername()))
            throw new IllegalStateException("username already exists");

        if (userRepo.existsByEmail(request.getEmail()))
            throw new IllegalStateException("email already exists");

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setEnabled(true);
        // אפשר למלא fullName/phone אם רוצים להוסיף לשדה ה־User

        User saved = userRepo.save(user);
        return toDto(saved);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        if (request == null) throw new IllegalArgumentException("request is required");
        if (request.getUsername() == null || request.getUsername().isBlank())
            throw new IllegalArgumentException("username is required");
        if (request.getPassword() == null || request.getPassword().isBlank())
            throw new IllegalArgumentException("password is required");

        User user = userRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (Boolean.FALSE.equals(user.getEnabled()))
            throw new AuthenticationException("User is disabled");

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
            throw new AuthenticationException("Invalid username or password");

        // TODO: replace with real JWT generation
        String fakeToken = "token_" + user.getId() + "_" + System.currentTimeMillis();

        var now = java.time.Instant.now();
        var expires = now.plusSeconds(3600);

        return AuthResponse.builder()
                .accessToken(fakeToken)
                .expiresIn(3600L)
                .issuedAt(now)
                .expiresAt(expires)
                .userId(user.getId())
                .username(user.getUsername())
                .roles(List.of(user.getRole()))
                .build();
    }

    // small custom runtime exception for auth failures (maps to 401)
    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) { super(message); }
    }
}