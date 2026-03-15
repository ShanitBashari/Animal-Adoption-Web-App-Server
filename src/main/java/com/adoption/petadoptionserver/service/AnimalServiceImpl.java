package com.adoption.petadoptionserver.service;

import com.adoption.petadoptionserver.dto.AnimalDto;
import com.adoption.petadoptionserver.enums.AdoptionRequestStatus;
import com.adoption.petadoptionserver.enums.AnimalStatus;
import com.adoption.petadoptionserver.interfaces.AnimalService;
import com.adoption.petadoptionserver.model.AdoptionRequest;
import com.adoption.petadoptionserver.model.Animal;
import com.adoption.petadoptionserver.model.Category;
import com.adoption.petadoptionserver.model.User;
import com.adoption.petadoptionserver.repository.AdoptionRequestRepository;
import com.adoption.petadoptionserver.repository.AnimalRepository;
import com.adoption.petadoptionserver.repository.CategoryRepository;
import com.adoption.petadoptionserver.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for animal-related business logic.
 * Handles creation, search, update, deletion, ownership validation,
 * and activation of animal listings.
 */
@Service
@Transactional
public class AnimalServiceImpl implements AnimalService {

    private static final Logger log = LoggerFactory.getLogger(AnimalServiceImpl.class);

    private static final String STATUS_INACTIVE = String.valueOf(AnimalStatus.INACTIVE);
    private static final String STATUS_AVAILABLE = String.valueOf(AnimalStatus.AVAILABLE);

    private final AnimalRepository animalRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final AdoptionRequestRepository adoptionRequestRepository;

    public AnimalServiceImpl(
            AnimalRepository animalRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            AdoptionRequestRepository adoptionRequestRepository
    ) {
        this.animalRepository = animalRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.adoptionRequestRepository = adoptionRequestRepository;
    }

    /**
     * Converts an Animal entity into an AnimalDto.
     *
     * @param animal the animal entity
     * @return mapped animal DTO
     */
    private AnimalDto toDto(Animal animal) {
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
        dto.setLocation(animal.getLocation());
        dto.setDescription(animal.getDescription());
        dto.setOwnerName(animal.getOwnerName());
        dto.setOwnerPhone(animal.getOwnerPhone());
        dto.setStatus(animal.getStatus());
        dto.setCategory(animal.getCategory() != null ? animal.getCategory().getName() : null);

        return dto;
    }

    /**
     * Copies DTO values into an Animal entity.
     * If the category does not exist, it is created automatically.
     *
     * @param entity the target animal entity
     * @param dto the source DTO
     */
    private void applyDtoToEntity(Animal entity, AnimalDto dto) {
        entity.setName(dto.getName());
        entity.setImage(dto.getImage());
        entity.setGender(dto.getGender());
        entity.setSize(dto.getSize());
        entity.setAge(dto.getAge());
        entity.setLocation(dto.getLocation());
        entity.setDescription(dto.getDescription());
        entity.setOwnerName(dto.getOwnerName());
        entity.setOwnerPhone(dto.getOwnerPhone());
        entity.setStatus(dto.getStatus());

        if (dto.getCategory() == null || dto.getCategory().isBlank()) {
            entity.setCategory(null);
            return;
        }

        String categoryName = dto.getCategory().trim();
        Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseGet(() -> {
                    log.info("Creating missing category with name={}", categoryName);
                    Category newCategory = new Category();
                    newCategory.setName(categoryName);
                    return categoryRepository.save(newCategory);
                });

        entity.setCategory(category);
    }

    /**
     * Finds a user by username or throws an exception if not found.
     *
     * @param username the username
     * @return user entity
     */
    private User mustFindUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found for username={}", username);
                    return new IllegalArgumentException("User not found: " + username);
                });
    }

    /**
     * Verifies that the given user is the owner of the given animal.
     *
     * @param username the username of the current user
     * @param animal the target animal
     */
    private void mustBeOwner(String username, Animal animal) {
        Long ownerId = animal.getOwnerUser() != null ? animal.getOwnerUser().getId() : null;
        Long currentUserId = mustFindUser(username).getId();

        if (ownerId == null || !ownerId.equals(currentUserId)) {
            log.warn("Forbidden animal operation. username={}, animalId={}", username, animal.getId());
            throw new IllegalStateException("Forbidden: not owner of this animal");
        }
    }

    /**
     * Returns all active animals except inactive ones.
     *
     * @return list of animal DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public List<AnimalDto> findAll() {
        return animalRepository.findByStatusOrderByIdDesc(String.valueOf(AnimalStatus.AVAILABLE))
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Returns an animal by ID.
     *
     * @param id the animal ID
     * @return optional animal DTO
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<AnimalDto> findById(Long id) {
        return animalRepository.findById(id).map(this::toDto);
    }

    /**
     * Searches animals by free-text query and optional category.
     *
     * @param q optional search query
     * @param category optional category filter
     * @return list of matching animal DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public List<AnimalDto> search(String q, String category) {
        return animalRepository.search(q, category)
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Creates a new animal listing for the given user.
     *
     * @param username the owner's username
     * @param dto animal data
     * @return created animal DTO
     */
    @Override
    public AnimalDto createForUser(String username, AnimalDto dto) {
        log.info("Creating animal for username={}", username);

        User user = mustFindUser(username);

        Animal entity = new Animal();
        applyDtoToEntity(entity, dto);
        entity.setOwnerUser(user);

        Animal savedAnimal = animalRepository.save(entity);

        log.info("Animal created successfully with id={} for username={}", savedAnimal.getId(), username);
        return toDto(savedAnimal);
    }

    /**
     * Updates an existing animal listing owned by the given user.
     *
     * @param username the owner's username
     * @param id the animal ID
     * @param dto updated animal data
     * @return updated animal DTO, or empty if not found
     */
    @Override
    public Optional<AnimalDto> updateForUser(String username, Long id, AnimalDto dto) {
        log.info("Updating animal with id={} for username={}", id, username);

        return animalRepository.findById(id).map(existingAnimal -> {
            mustBeOwner(username, existingAnimal);
            applyDtoToEntity(existingAnimal, dto);
            Animal savedAnimal = animalRepository.save(existingAnimal);
            log.info("Animal updated successfully with id={}", id);
            return toDto(savedAnimal);
        }).or(() -> {
            log.warn("Animal not found for update. id={}", id);
            return Optional.empty();
        });
    }

    /**
     * Deletes an animal owned by the given user.
     * If the animal already has adoption requests, it is marked as INACTIVE instead of being deleted.
     *
     * @param username the owner's username
     * @param id the animal ID
     * @return true if handled successfully, otherwise false
     */
    @Override
    public boolean deleteForUser(String username, Long id) {
        log.info("Deleting animal with id={} for username={}", id, username);

        Optional<Animal> animalOptional = animalRepository.findById(id);
        if (animalOptional.isEmpty()) {
            log.warn("Animal not found for deletion. id={}", id);
            return false;
        }

        Animal animal = animalOptional.get();
        mustBeOwner(username, animal);

        boolean hasRequests = adoptionRequestRepository.existsByAnimal_Id(id);

        if (hasRequests) {
            List<AdoptionRequest> pendingRequests =
                    adoptionRequestRepository.findByAnimal_IdAndStatus(id, String.valueOf(AdoptionRequestStatus.PENDING));

            for (AdoptionRequest request : pendingRequests) {
                request.setStatus(String.valueOf(AdoptionRequestStatus.REJECTED));
                request.setReason("This animal listing was removed by the owner");
            }

            adoptionRequestRepository.saveAll(pendingRequests);

            animal.setStatus(String.valueOf(AnimalStatus.INACTIVE));
            animalRepository.save(animal);

            log.info("Animal with id={} marked as INACTIVE and pending requests were rejected", id);
        } else {
            animalRepository.deleteById(id);
            log.info("Animal deleted successfully with id={}", id);
        }

        return true;
    }

    /**
     * Returns all animals owned by the given user.
     *
     * @param username the username
     * @return list of owned animal DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public List<AnimalDto> findMine(String username) {
        User user = mustFindUser(username);

        return animalRepository.findByOwnerUser_IdOrderByIdDesc(user.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Reactivates an inactive animal listing owned by the given user.
     *
     * @param username the owner's username
     * @param id the animal ID
     * @return updated animal DTO, or empty if not found
     */
    @Override
    public Optional<AnimalDto> activateForUser(String username, Long id) {
        log.info("Activating animal with id={} for username={}", id, username);

        return animalRepository.findById(id).map(existingAnimal -> {
            mustBeOwner(username, existingAnimal);

            if (!String.valueOf(AnimalStatus.INACTIVE).equals(existingAnimal.getStatus())) {
                throw new IllegalStateException("Only inactive animals can be activated");
            }

            existingAnimal.setStatus(STATUS_AVAILABLE);
            Animal savedAnimal = animalRepository.save(existingAnimal);
            log.info("Animal activated successfully with id={}", id);
            return toDto(savedAnimal);
        }).or(() -> {
            log.warn("Animal not found for activation. id={}", id);
            return Optional.empty();
        });
    }
}