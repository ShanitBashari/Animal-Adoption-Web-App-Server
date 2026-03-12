package com.adoption.petadoptionserver.service;

import com.adoption.petadoptionserver.dto.UserDto;
import com.adoption.petadoptionserver.enums.UserRole;
import com.adoption.petadoptionserver.interfaces.UserService;
import com.adoption.petadoptionserver.model.User;
import com.adoption.petadoptionserver.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for user management operations.
 * Handles user creation, retrieval, and deletion.
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Converts a User entity into a UserDto.
     *
     * @param user the user entity
     * @return mapped user DTO
     */
    private UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRoles(List.of(user.getRole()));
        dto.setEnabled(user.getEnabled());
        dto.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        dto.setFullName(null);
        dto.setPhone(null);

        return dto;
    }

    /**
     * Converts a UserDto into a User entity.
     *
     * @param dto the user DTO
     * @return mapped user entity
     */
    private User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());

        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            user.setRole(dto.getRoles().get(0));
        } else {
            user.setRole(String.valueOf(UserRole.USER));
        }

        user.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);

        return user;
    }

    /**
     * Creates a new user after validating required fields and uniqueness constraints.
     *
     * @param userDto the user data
     * @return created user DTO
     */
    @Override
    public UserDto createUser(UserDto userDto) {
        if (userDto == null) {
            throw new IllegalArgumentException("userDto is required");
        }
        if (userDto.getUsername() == null || userDto.getUsername().isBlank()) {
            throw new IllegalArgumentException("username is required");
        }
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        if (userDto.getPassword() == null || userDto.getPassword().isBlank()) {
            throw new IllegalArgumentException("password is required");
        }

        String username = userDto.getUsername().trim();
        String email = userDto.getEmail().trim().toLowerCase();

        log.info("Creating user with username={}", username);

        if (userRepository.existsByUsername(username)) {
            log.warn("User creation failed. Username already exists: {}", username);
            throw new IllegalStateException("username already exists");
        }

        if (userRepository.existsByEmail(email)) {
            log.warn("User creation failed. Email already exists: {}", email);
            throw new IllegalStateException("email already exists");
        }

        userDto.setUsername(username);
        userDto.setEmail(email);

        User user = toEntity(userDto);
        user.setPasswordHash(passwordEncoder.encode(userDto.getPassword()));

        User savedUser = userRepository.save(user);

        log.info("User created successfully with id={} username={}", savedUser.getId(), savedUser.getUsername());
        return toDto(savedUser);
    }

    /**
     * Returns a user by ID.
     *
     * @param id the user ID
     * @return optional user DTO
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findById(Long id) {
        return userRepository.findById(id).map(this::toDto);
    }

    /**
     * Returns a user by username.
     *
     * @param username the username
     * @return optional user DTO
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findByUsername(String username) {
        return userRepository.findByUsername(username).map(this::toDto);
    }

    /**
     * Returns all users.
     *
     * @return list of user DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Deletes a user by ID.
     *
     * @param id the user ID
     * @return true if deleted, otherwise false
     */
    @Override
    public boolean deleteUser(Long id) {
        log.info("Deleting user with id={}", id);

        if (!userRepository.existsById(id)) {
            log.warn("User not found for deletion. id={}", id);
            return false;
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully with id={}", id);
        return true;
    }
}