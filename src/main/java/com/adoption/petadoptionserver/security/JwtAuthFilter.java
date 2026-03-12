package com.adoption.petadoptionserver.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter executed once per request.
 * Extracts the Bearer token from the Authorization header,
 * validates it, loads the matching user, and stores the authentication
 * object in the Spring Security context.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Intercepts the incoming request and tries to authenticate the user
     * based on the JWT Bearer token.
     *
     * If the token is missing, invalid, or cannot be matched to a valid user,
     * the request continues without authentication.
     *
     * @param request the current HTTP request
     * @param response the current HTTP response
     * @param filterChain the remaining filter chain
     * @throws ServletException if a servlet-related error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring("Bearer ".length()).trim();

            if (!jwtService.isValid(token)) {
                log.warn("Invalid JWT token for request {} {}", request.getMethod(), request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            String username = jwtService.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                validateUserIdClaim(token, userDetails, request);

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                log.info("JWT authentication successful for username={}", username);
            }

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            log.error(
                    "Unexpected error during JWT authentication for request {} {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    ex
            );
            throw ex;
        }
    }

    /**
     * Validates that the userId claim inside the token matches
     * the authenticated principal ID loaded from the database.
     *
     * @param token the JWT token
     * @param userDetails the loaded user details
     * @param request the current request
     */
    private void validateUserIdClaim(String token, UserDetails userDetails, HttpServletRequest request) {
        try {
            Claims claims = jwtService.parseClaims(token);
            Object claimUserId = claims.get("userId");

            if (claimUserId != null && userDetails instanceof AppUserDetails appUserDetails) {
                Long jwtUserId = Long.valueOf(String.valueOf(claimUserId));
                Long principalUserId = appUserDetails.getId();

                if (!jwtUserId.equals(principalUserId)) {
                    log.warn(
                            "JWT userId mismatch for request {} {}. tokenUserId={}, principalUserId={}",
                            request.getMethod(),
                            request.getRequestURI(),
                            jwtUserId,
                            principalUserId
                    );
                    throw new IllegalArgumentException("JWT user ID mismatch");
                }
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Failed to validate JWT userId claim", ex);
            throw new IllegalArgumentException("Invalid JWT claims");
        }
    }
}