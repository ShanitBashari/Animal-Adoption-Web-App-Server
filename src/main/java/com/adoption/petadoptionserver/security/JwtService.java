package com.adoption.petadoptionserver.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.*;

@Service
public class JwtService {

    private final Key signingKey;
    private final long accessTtlSeconds;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.accessTtlSeconds:3600}") long accessTtlSeconds
    ) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(toBase64(secret)));
        this.accessTtlSeconds = accessTtlSeconds;
    }

    private String toBase64(String s) {
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    public long getAccessTtlSeconds() {
        return accessTtlSeconds;
    }

    public String generateAccessToken(String username, Long userId, List<String> roles) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTtlSeconds);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roles", roles);

        return Jwts.builder()
                .setSubject(username)
                .addClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object roles = parseClaims(token).get("roles");
        if (roles instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object o : list) out.add(String.valueOf(o));
            return out;
        }
        return List.of();
    }
}