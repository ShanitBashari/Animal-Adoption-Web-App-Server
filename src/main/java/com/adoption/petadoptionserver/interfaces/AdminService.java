package com.adoption.petadoptionserver.interfaces;

import com.adoption.petadoptionserver.dto.AnimalDto;
import com.adoption.petadoptionserver.dto.UserDto;

import java.util.List;
import java.util.Optional;

public interface AdminService {
    List<UserDto> listUsers();
    Optional<UserDto> deactivateUser(Long userId);
    Optional<UserDto> activateUser(Long userId);
    boolean removeAnimal(Long animalId);
    List<AnimalDto> listAnimals();
    AnimalDto approveAnimal(Long animalId);
    AnimalDto rejectAnimal(Long animalId, String reason);
}