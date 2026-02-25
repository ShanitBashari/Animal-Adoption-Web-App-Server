package com.adoption.petadoptionserver.service;

import com.adoption.petadoptionserver.dto.UserDto;
import com.adoption.petadoptionserver.interfaces.AdminService;
import com.adoption.petadoptionserver.model.User;
import com.adoption.petadoptionserver.repository.UserRepository;
import com.adoption.petadoptionserver.repository.AnimalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepo;
    private final AnimalRepository animalRepo;

    public AdminServiceImpl(UserRepository userRepo, AnimalRepository animalRepo) {
        this.userRepo = userRepo;
        this.animalRepo = animalRepo;
    }

    private UserDto toDto(User user) {
        if (user == null) return null;

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRoles(user.getRole() != null ? java.util.List.of(user.getRole()) : java.util.List.of("USER"));
        dto.setEnabled(Boolean.TRUE.equals(user.getEnabled()));
        dto.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> listUsers() {
        return userRepo.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserDto> deactivateUser(Long userId) {
        return userRepo.findById(userId).map(user -> {
            user.setEnabled(false);
            User saved = userRepo.save(user);
            return toDto(saved);
        });
    }

    @Override
    public Optional<UserDto> activateUser(Long userId) {
        return userRepo.findById(userId).map(user -> {
            user.setEnabled(true);
            User saved = userRepo.save(user);
            return toDto(saved);
        });
    }

    @Override
    public boolean removeAnimal(Long animalId) {
        if (!animalRepo.existsById(animalId)) {
            return false;
        }
        animalRepo.deleteById(animalId);
        return true;
    }
}