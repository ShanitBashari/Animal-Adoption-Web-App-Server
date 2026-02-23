package com.adoption.petadoptionserver.service;

import com.adoption.petadoptionserver.dto.UserDto;
import com.adoption.petadoptionserver.interfaces.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Profile("mock")
public class MockUserService implements UserService {

    private final Map<Long, UserDto> store = new LinkedHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    @PostConstruct
    public void init() {
        UserDto u1 = new UserDto();
        u1.setId(idGen.getAndIncrement());
        u1.setUsername("david");
        u1.setEmail("david@example.com");
        u1.setFullName("David Cohen");
        u1.setPhone("050-1234567");
        u1.setRoles(List.of("ROLE_USER"));
        u1.setEnabled(true);
        u1.setCreatedAt(Instant.now().toString());
        store.put(u1.getId(), u1);

        UserDto admin = new UserDto();
        admin.setId(idGen.getAndIncrement());
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setFullName("Admin");
        admin.setPhone(null);
        admin.setRoles(List.of("ROLE_ADMIN", "ROLE_USER"));
        admin.setEnabled(true);
        admin.setCreatedAt(Instant.now().toString());
        store.put(admin.getId(), admin);
    }

    @Override
    public List<UserDto> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<UserDto> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<UserDto> findByUsername(String username) {
        return store.values().stream()
                .filter(u -> u.getUsername() != null && u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    @Override
    public UserDto create(UserDto dto) {
        Long id = idGen.getAndIncrement();
        dto.setId(id);
        if (dto.getRoles() == null) dto.setRoles(List.of("ROLE_USER"));
        dto.setEnabled(true);
        if (dto.getCreatedAt() == null) dto.setCreatedAt(Instant.now().toString());
        store.put(id, dto);
        return dto;
    }

    @Override
    public Optional<UserDto> update(Long id, UserDto dto) {
        if (!store.containsKey(id)) return Optional.empty();
        dto.setId(id);
        store.put(id, dto);
        return Optional.of(dto);
    }

    @Override
    public boolean delete(Long id) {
        return store.remove(id) != null;
    }
}