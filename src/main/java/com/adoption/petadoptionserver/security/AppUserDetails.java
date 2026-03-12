package com.adoption.petadoptionserver.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Custom implementation of Spring Security's UserDetails.
 * Represents the authenticated application user inside the security context.
 */
public class AppUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Creates a new authenticated user principal.
     *
     * @param id the user ID
     * @param username the username used for authentication
     * @param password the encoded password
     * @param enabled whether the user account is enabled
     * @param authorities the granted roles/authorities of the user
     */
    public AppUserDetails(
            Long id,
            String username,
            String password,
            boolean enabled,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    /**
     * Returns the internal database ID of the authenticated user.
     *
     * @return user ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the authorities granted to the user.
     *
     * @return collection of granted authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Returns the encoded password used by Spring Security.
     *
     * @return encoded password
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the username used by Spring Security.
     *
     * @return username
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Indicates whether the user account is not expired.
     *
     * @return always true for this implementation
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user account is not locked.
     *
     * @return always true for this implementation
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user credentials are not expired.
     *
     * @return always true for this implementation
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled.
     *
     * @return true if the user is enabled, otherwise false
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}