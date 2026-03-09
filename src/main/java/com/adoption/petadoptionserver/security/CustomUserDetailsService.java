package com.adoption.petadoptionserver.security;

import com.adoption.petadoptionserver.model.User;
import com.adoption.petadoptionserver.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    public CustomUserDetailsService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String role = u.getRole() == null ? "USER" : u.getRole().trim().toUpperCase();
        List<SimpleGrantedAuthority> auths = List.of(new SimpleGrantedAuthority("ROLE_" + role));

        boolean enabled = !Boolean.FALSE.equals(u.getEnabled());

        return new AppUserDetails(
                u.getId(),
                u.getUsername(),
                u.getPasswordHash(),
                enabled,
                auths
        );
    }
}