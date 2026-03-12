package com.adoption.petadoptionserver.security;

import com.adoption.petadoptionserver.enums.UserRole;
import com.adoption.petadoptionserver.model.User;
import com.adoption.petadoptionserver.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Custom implementation of Spring Security's UserDetailsService.
 * Loads user authentication data from the database by username.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by username and converts it into a Spring Security UserDetails object.
     *
     * @param username the username used during authentication
     * @return authenticated user details
     * @throws UsernameNotFoundException if no user is found with the given username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user details for username={}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found for username={}", username);
                    return new UsernameNotFoundException("User not found");
                });

        String role = user.getRole() == null
                ? String.valueOf(UserRole.USER)
                : user.getRole().trim().toUpperCase();

        List<SimpleGrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + role));

        boolean enabled = !Boolean.FALSE.equals(user.getEnabled());

        log.info("User details loaded successfully for username={} with role={}", username, role);

        return new AppUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                enabled,
                authorities
        );
    }
}