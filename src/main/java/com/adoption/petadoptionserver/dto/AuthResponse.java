package com.adoption.petadoptionserver.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    // JWT access token
    private String accessToken;

    // optional (later)
    private String refreshToken;

    // always Bearer but NOT inside builder bug
    @Builder.Default
    private String tokenType = "Bearer";

    // seconds until expiration
    private Long expiresIn;

    // metadata
    private Instant issuedAt;
    private Instant expiresAt;

    // user info
    private Long userId;
    private String username;

    @Builder.Default
    private List<String> roles = List.of();
}