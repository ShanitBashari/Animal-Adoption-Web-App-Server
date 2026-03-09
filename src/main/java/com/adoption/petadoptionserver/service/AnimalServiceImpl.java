package com.adoption.petadoptionserver.service;

import com.adoption.petadoptionserver.dto.AnimalDto;
import com.adoption.petadoptionserver.interfaces.AnimalService;
import com.adoption.petadoptionserver.model.Animal;
import com.adoption.petadoptionserver.model.Category;
import com.adoption.petadoptionserver.model.User;
import com.adoption.petadoptionserver.repository.AdoptionRequestRepository;
import com.adoption.petadoptionserver.repository.AnimalRepository;
import com.adoption.petadoptionserver.repository.CategoryRepository;
import com.adoption.petadoptionserver.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AnimalServiceImpl implements AnimalService {

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

    private AnimalDto toDto(Animal a) {
        if (a == null) return null;

        AnimalDto dto = new AnimalDto();
        dto.setId(a.getId());
        dto.setOwnerUserId(a.getOwnerUser() != null ? a.getOwnerUser().getId() : null);
        dto.setName(a.getName());
        dto.setImage(a.getImage());
        dto.setGender(a.getGender());
        dto.setSize(a.getSize());
        dto.setAge(a.getAge());
        dto.setLocation(a.getLocation());
        dto.setDescription(a.getDescription());
        dto.setOwnerName(a.getOwnerName());
        dto.setOwnerPhone(a.getOwnerPhone());
        dto.setStatus(a.getStatus());
        dto.setCategory(a.getCategory() != null ? a.getCategory().getName() : null);

        return dto;
    }

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
        } else {
            String catName = dto.getCategory().trim();
            Category category = categoryRepository.findByNameIgnoreCase(catName)
                    .orElseGet(() -> {
                        Category c = new Category();
                        c.setName(catName);
                        return categoryRepository.save(c);
                    });
            entity.setCategory(category);
        }
    }

    private User mustFindUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    private void mustBeOwner(String username, Animal animal) {
        Long ownerId = animal.getOwnerUser() != null ? animal.getOwnerUser().getId() : null;
        Long userId = mustFindUser(username).getId();
        if (ownerId == null || !ownerId.equals(userId)) {
            throw new RuntimeException("Forbidden: not owner of this animal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnimalDto> findAll() {
        return animalRepository.findByStatusNotOrderByIdDesc("INACTIVE")
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AnimalDto> findById(Long id) {
        return animalRepository.findById(id).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnimalDto> search(String q, String category) {
        return animalRepository.search(q, category)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AnimalDto createForUser(String username, AnimalDto dto) {
        User u = mustFindUser(username);

        Animal entity = new Animal();
        applyDtoToEntity(entity, dto);
        entity.setOwnerUser(u);

        Animal saved = animalRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public Optional<AnimalDto> updateForUser(String username, Long id, AnimalDto dto) {
        return animalRepository.findById(id).map(existing -> {
            mustBeOwner(username, existing);
            applyDtoToEntity(existing, dto);
            Animal saved = animalRepository.save(existing);
            return toDto(saved);
        });
    }

    @Override
    public boolean deleteForUser(String username, Long id) {
        Optional<Animal> opt = animalRepository.findById(id);
        if (opt.isEmpty()) return false;

        Animal a = opt.get();
        mustBeOwner(username, a);

        boolean hasRequests = adoptionRequestRepository.existsByAnimal_Id(id);

        if (hasRequests) {
            a.setStatus("INACTIVE");
            animalRepository.save(a);
        } else {
            animalRepository.deleteById(id);
        }

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnimalDto> findMine(String username) {
        User u = mustFindUser(username);
        return animalRepository.findByOwnerUser_IdOrderByIdDesc(u.getId())
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AnimalDto> activateForUser(String username, Long id) {
        return animalRepository.findById(id).map(existing -> {
            mustBeOwner(username, existing);
            existing.setStatus("AVAILABLE");
            Animal saved = animalRepository.save(existing);
            return toDto(saved);
        });
    }
}