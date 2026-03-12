package com.adoption.petadoptionserver.service;

import com.adoption.petadoptionserver.dto.AnimalDto;
import com.adoption.petadoptionserver.dto.UserDto;
import com.adoption.petadoptionserver.enums.AnimalStatus;
import com.adoption.petadoptionserver.enums.UserRole;
import com.adoption.petadoptionserver.interfaces.AdminService;
import com.adoption.petadoptionserver.model.Animal;
import com.adoption.petadoptionserver.model.User;
import com.adoption.petadoptionserver.repository.AnimalRepository;
import com.adoption.petadoptionserver.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation responsible for administrative operations
 * related to users and animals.
 */
@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

    private final UserRepository userRepository;
    private final AnimalRepository animalRepository;

    public AdminServiceImpl(UserRepository userRepository, AnimalRepository animalRepository) {
        this.userRepository = userRepository;
        this.animalRepository = animalRepository;
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
        dto.setRoles(user.getRole() != null ? List.of(user.getRole()) : List.of(String.valueOf(UserRole.USER)));
        dto.setEnabled(Boolean.TRUE.equals(user.getEnabled()));
        dto.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);

        return dto;
    }

    /**
     * Converts an Animal entity into an AnimalDto.
     *
     * @param animal the animal entity
     * @return mapped animal DTO
     */
    private AnimalDto toAnimalDto(Animal animal) {
        if (animal == null) {
            return null;
        }

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

    /**
     * Returns all users in the system.
     *
     * @return list of user DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> listUsers() {
        log.info("Listing all users");
        return userRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Deactivates a user account by ID.
     *
     * @param userId the user ID
     * @return updated user DTO, or empty if not found
     */
    @Override
    public Optional<UserDto> deactivateUser(Long userId) {
        log.info("Deactivating user with id={}", userId);

        return userRepository.findById(userId).map(user -> {
            user.setEnabled(false);
            User savedUser = userRepository.save(user);
            log.info("User deactivated successfully with id={}", userId);
            return toDto(savedUser);
        }).or(() -> {
            log.warn("User not found for deactivation. id={}", userId);
            return Optional.empty();
        });
    }

    /**
     * Activates a user account by ID.
     *
     * @param userId the user ID
     * @return updated user DTO, or empty if not found
     */
    @Override
    public Optional<UserDto> activateUser(Long userId) {
        log.info("Activating user with id={}", userId);

        return userRepository.findById(userId).map(user -> {
            user.setEnabled(true);
            User savedUser = userRepository.save(user);
            log.info("User activated successfully with id={}", userId);
            return toDto(savedUser);
        }).or(() -> {
            log.warn("User not found for activation. id={}", userId);
            return Optional.empty();
        });
    }

    /**
     * Removes an animal by ID.
     *
     * @param animalId the animal ID
     * @return true if deleted, otherwise false
     */
    @Override
    public boolean removeAnimal(Long animalId) {
        log.info("Removing animal with id={}", animalId);

        if (!animalRepository.existsById(animalId)) {
            log.warn("Animal not found for deletion. id={}", animalId);
            return false;
        }

        animalRepository.deleteById(animalId);
        log.info("Animal removed successfully with id={}", animalId);
        return true;
    }

    /**
     * Returns all animals for administrative management.
     *
     * @return list of animal DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public List<AnimalDto> listAnimals() {
        log.info("Listing all animals for admin");
        return animalRepository.findAll()
                .stream()
                .map(this::toAnimalDto)
                .toList();
    }

    /**
     * Approves an animal listing by changing its status to AVAILABLE.
     *
     * @param animalId the animal ID
     * @return updated animal DTO
     */
    @Override
    public AnimalDto approveAnimal(Long animalId) {
        log.info("Approving animal with id={}", animalId);

        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(() -> {
                    log.warn("Animal not found for approval. id={}", animalId);
                    return new IllegalArgumentException("Animal not found");
                });

        animal.setStatus(String.valueOf(AnimalStatus.AVAILABLE));
        Animal savedAnimal = animalRepository.save(animal);

        log.info("Animal approved successfully with id={}", animalId);
        return toAnimalDto(savedAnimal);
    }

    /**
     * Rejects an animal listing by changing its status to REJECTED.
     *
     * @param animalId the animal ID
     * @param reason optional rejection reason
     * @return updated animal DTO
     */
    @Override
    public AnimalDto rejectAnimal(Long animalId, String reason) {
        log.info("Rejecting animal with id={}", animalId);

        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(() -> {
                    log.warn("Animal not found for rejection. id={}", animalId);
                    return new IllegalArgumentException("Animal not found");
                });

        animal.setStatus(String.valueOf(AnimalStatus.REJECTED));
        Animal savedAnimal = animalRepository.save(animal);

        log.info("Animal rejected successfully with id={}", animalId);
        return toAnimalDto(savedAnimal);
    }
}