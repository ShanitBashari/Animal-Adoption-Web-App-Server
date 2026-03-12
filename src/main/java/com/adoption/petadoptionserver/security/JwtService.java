package com.adoption.petadoptionserver.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.*;

/**
 * Service responsible for generating and validating JWT tokens.
 * Handles token creation, claims parsing, username extraction,
 * and role extraction.
 */
@Service
public class JwtService {

    private final Key signingKey;
    private final long accessTtlSeconds;

    /**
     * Creates the JWT service with the configured secret key and token lifetime.
     *
     * @param secret the application JWT secret
     * @param accessTtlSeconds access token lifetime in seconds
     */
    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.accessTtlSeconds:3600}") long accessTtlSeconds
    ) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(toBase64(secret)));
        this.accessTtlSeconds = accessTtlSeconds;
    }

    /**
     * Returns the configured access token lifetime in seconds.
     *
     * @return access token TTL in seconds
     */
    public long getAccessTtlSeconds() {
        return accessTtlSeconds;
    }

    /**
     * Generates a signed JWT access token for the given user.
     *
     * @param username the authenticated username
     * @param userId the user ID
     * @param roles the user roles
     * @return signed JWT token
     */
    public String generateAccessToken(String username, Long userId, List<String> roles) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessTtlSeconds);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roles", roles);

        return Jwts.builder()
                .setSubject(username)
                .addClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiresAt))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Parses and returns all claims from the given token.
     *
     * @param token the JWT token
     * @return parsed claims
     * @throws JwtException if the token is invalid
     */
    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks whether the given token is valid.
     *
     * @param token the JWT token
     * @return true if valid, otherwise false
     */
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Extracts the username (subject) from the token.
     *
     * @param token the JWT token
     * @return username stored in the token
     */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extracts user roles from the token claims.
     *
     * @param token the JWT token
     * @return list of roles, or an empty list if not found
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object roles = parseClaims(token).get("roles");

        if (roles instanceof List<?> list) {
            List<String> extractedRoles = new ArrayList<>();
            for (Object role : list) {
                extractedRoles.add(String.valueOf(role));
            }
            return extractedRoles;
        }

        return List.of();
    }

    /**
     * Encodes a plain string as Base64.
     * Used to adapt the configured secret into the format expected by the JWT library.
     *
     * @param value plain string value
     * @return Base64-encoded string
     */
    private String toBase64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}