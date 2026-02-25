package com.adoption.petadoptionserver.service;

import com.adoption.petadoptionserver.dto.UserDto;
import com.adoption.petadoptionserver.interfaces.UserService;
import com.adoption.petadoptionserver.model.User;
import com.adoption.petadoptionserver.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    private UserDto toDto(User user) {
        if (user == null) return null;

        UserDto dto = UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(java.util.List.of(user.getRole()))
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .fullName(null)
                .phone(null)
                .build();

        // אם יש צורך למלא fullName/phone מה־User entity — הוסף כאן
        return dto;
    }

    private User toEntity(UserDto dto) {
        if (dto == null) return null;

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());

        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            user.setRole(dto.getRoles().get(0));
        } else {
            user.setRole("USER");
        }

        if (dto.getEnabled() != null) {
            user.setEnabled(dto.getEnabled());
        } else {
            user.setEnabled(true);
        }

        // שים לב: סיסמת הטקסט אינה נשמרת כאן — נשמרת רק כ־hash בהמשך
        return user;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userDto == null) throw new IllegalArgumentException("userDto is required");

        if (userDto.getUsername() == null || userDto.getUsername().isBlank())
            throw new IllegalArgumentException("username is required");

        if (userDto.getEmail() == null || userDto.getEmail().isBlank())
            throw new IllegalArgumentException("email is required");

        // בדיקות ייחודיות
        if (repo.existsByUsername(userDto.getUsername()))
            throw new IllegalStateException("username already exists");

        if (repo.existsByEmail(userDto.getEmail()))
            throw new IllegalStateException("email already exists");

        if (userDto.getPassword() == null || userDto.getPassword().isBlank())
            throw new IllegalArgumentException("password is required");

        User user = toEntity(userDto);
        user.setPasswordHash(passwordEncoder.encode(userDto.getPassword()));

        User saved = repo.save(user);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findById(Long id) {
        return repo.findById(id).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findByUsername(String username) {
        return repo.findByUsername(username).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return repo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public boolean deleteUser(Long id) {
        if (!repo.existsById(id)) {
            return false;
        }
        repo.deleteById(id);
        return true;
    }
}