package com.adoption.petadoptionserver.interfaces;

// AdminService
import com.adoption.petadoptionserver.dto.UserDto;

import java.util.List;
import java.util.Optional;

public interface AdminService {
    List<UserDto> listUsers();
    Optional<UserDto> promoteToAdmin(Long userId);
    Optional<UserDto> deactivateUser(Long userId);
    boolean removeAnimal(Long animalId);
}