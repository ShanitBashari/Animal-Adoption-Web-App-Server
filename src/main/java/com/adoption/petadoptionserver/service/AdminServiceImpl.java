package com.adoption.petadoptionserver.service;

import com.adoption.petadoptionserver.dto.AnimalDto;
import com.adoption.petadoptionserver.dto.UserDto;
import com.adoption.petadoptionserver.interfaces.AdminService;
import com.adoption.petadoptionserver.model.Animal;
import com.adoption.petadoptionserver.model.User;
import com.adoption.petadoptionserver.repository.UserRepository;
import com.adoption.petadoptionserver.repository.AnimalRepository;
import com.adoption.petadoptionserver.enums.AnimalStatus;
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

    private AnimalDto toAnimalDto(Animal animal) {
        if (animal == null) return null;

        AnimalDto dto = new AnimalDto();
        dto.setId(animal.getId());
        dto.setOwnerUserId(animal.getOwnerUser() != null ? animal.getOwnerUser().getId() : null);
        dto.setName(animal.getName());
        dto.setImage(animal.getImage());
        dto.setGender(animal.getGender());
        dto.setSize(animal.getSize());
        dto.setAge(animal.getAge());
        dto.setCategory(animal.getCategory() != null ? animal.getCategory().getName() : null);
        dto.setLocation(animal.getLocation());
        dto.setDescription(animal.getDescription());
        dto.setOwnerName(animal.getOwnerName());
        dto.setOwnerPhone(animal.getOwnerPhone());
        dto.setStatus(animal.getStatus());
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

    @Override
    @Transactional(readOnly = true)
    public List<AnimalDto> listAnimals() {
        return animalRepo.findAll().stream()
                .map(this::toAnimalDto)
                .collect(Collectors.toList());
    }

    @Override
    public AnimalDto approveAnimal(Long animalId) {
        Animal animal = animalRepo.findById(animalId)
                .orElseThrow(() -> new RuntimeException("Animal not found"));

        animal.setStatus(String.valueOf(AnimalStatus.AVAILABLE));

        Animal saved = animalRepo.save(animal);
        return toAnimalDto(saved);
    }

    @Override
    public AnimalDto rejectAnimal(Long animalId, String reason) {
        Animal animal = animalRepo.findById(animalId)
                .orElseThrow(() -> new RuntimeException("Animal not found"));

        animal.setStatus(String.valueOf(AnimalStatus.REJECTED));

        Animal saved = animalRepo.save(animal);
        return toAnimalDto(saved);
    }
}