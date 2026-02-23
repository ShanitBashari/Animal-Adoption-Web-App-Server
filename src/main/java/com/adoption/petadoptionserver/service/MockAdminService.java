package com.adoption.petadoptionserver.service;

// MockAdminService

import com.adoption.petadoptionserver.dto.UserDto;
import com.adoption.petadoptionserver.interfaces.AdminService;
import com.adoption.petadoptionserver.interfaces.AnimalService;
import com.adoption.petadoptionserver.interfaces.UserService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Profile("mock")
public class MockAdminService implements AdminService {

    private final UserService userService;
    private final AnimalService animalService;

    public MockAdminService(UserService userService, AnimalService animalService) {
        this.userService = userService;
        this.animalService = animalService;
    }

    @Override
    public List<UserDto> listUsers() {
        return userService.findAll();
    }

    @Override
    public Optional<UserDto> promoteToAdmin(Long userId) {
        Optional<UserDto> u = userService.findById(userId);
        if (u.isEmpty()) return Optional.empty();
        UserDto dto = u.get();
        List<String> roles = dto.getRoles();
        if (!roles.contains("ROLE_ADMIN")) {
            roles.add("ROLE_ADMIN");
            dto.setRoles(roles);
            userService.update(userId, dto);
        }
        return Optional.of(dto);
    }

    @Override
    public Optional<UserDto> deactivateUser(Long userId) {
        Optional<UserDto> u = userService.findById(userId);
        if (u.isEmpty()) return Optional.empty();
        UserDto dto = u.get();
        dto.setEnabled(false);
        userService.update(userId, dto);
        return Optional.of(dto);
    }

    @Override
    public boolean removeAnimal(Long animalId) {
        return animalService.delete(animalId);
    }
}