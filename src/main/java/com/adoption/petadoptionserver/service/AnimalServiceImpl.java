package com.adoption.petadoptionserver.service;

import com.adoption.petadoptionserver.dto.AnimalDto;
import com.adoption.petadoptionserver.interfaces.AnimalService;
import com.adoption.petadoptionserver.model.Animal;
import com.adoption.petadoptionserver.model.Category;
import com.adoption.petadoptionserver.repository.AnimalRepository;
import com.adoption.petadoptionserver.repository.CategoryRepository;
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

    public AnimalServiceImpl(AnimalRepository animalRepository,
                             CategoryRepository categoryRepository) {
        this.animalRepository = animalRepository;
        this.categoryRepository = categoryRepository;
    }

    private AnimalDto toDto(Animal a) {
        if (a == null) return null;

        AnimalDto dto = new AnimalDto();
        dto.setId(a.getId());
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

        // category by name (creates if missing)
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

    @Override
    @Transactional(readOnly = true)
    public List<AnimalDto> findAll() {
        return animalRepository.findAll()
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
    public AnimalDto create(AnimalDto dto) {
        Animal entity = new Animal();
        applyDtoToEntity(entity, dto);
        Animal saved = animalRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public Optional<AnimalDto> update(Long id, AnimalDto dto) {
        return animalRepository.findById(id).map(existing -> {
            applyDtoToEntity(existing, dto);
            Animal saved = animalRepository.save(existing);
            return toDto(saved);
        });
    }

    @Override
    public boolean delete(Long id) {
        if (!animalRepository.existsById(id)) return false;
        animalRepository.deleteById(id);
        return true;
    }
}