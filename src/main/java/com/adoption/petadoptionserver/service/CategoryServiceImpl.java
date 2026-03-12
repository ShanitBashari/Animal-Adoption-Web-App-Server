package com.adoption.petadoptionserver.service;

import com.adoption.petadoptionserver.dto.CategoryDto;
import com.adoption.petadoptionserver.interfaces.CategoryService;
import com.adoption.petadoptionserver.model.Category;
import com.adoption.petadoptionserver.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for category management.
 * Supports retrieving, creating, deactivating, and reactivating categories.
 */
@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Converts a Category entity into a CategoryDto.
     *
     * @param category the category entity
     * @return mapped category DTO
     */
    private CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryDto(category.getId(), category.getName(), category.getActive());
    }

    /**
     * Returns all categories.
     *
     * @return list of category DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Returns only active categories.
     *
     * @return list of active category DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> findAllActive() {
        return categoryRepository.findAll()
                .stream()
                .filter(Category::getActive)
                .map(this::toDto)
                .toList();
    }

    /**
     * Returns a category by ID.
     *
     * @param id the category ID
     * @return optional category DTO
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDto> findById(Long id) {
        return categoryRepository.findById(id).map(this::toDto);
    }

    /**
     * Returns a category by name.
     *
     * @param name the category name
     * @return optional category DTO
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDto> findByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }

        return categoryRepository.findByNameIgnoreCase(name.trim()).map(this::toDto);
    }

    /**
     * Creates a new category.
     * If an inactive category with the same name already exists, it is reactivated instead.
     *
     * @param dto the category data
     * @return created or reactivated category DTO
     */
    @Override
    public CategoryDto create(CategoryDto dto) {
        if (dto == null || dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }

        String categoryName = dto.getName().trim();
        log.info("Creating category with name={}", categoryName);

        Optional<Category> existingCategoryOptional = categoryRepository.findByNameIgnoreCase(categoryName);

        if (existingCategoryOptional.isPresent()) {
            Category existingCategory = existingCategoryOptional.get();

            if (Boolean.TRUE.equals(existingCategory.getActive())) {
                log.warn("Category already exists with name={}", categoryName);
                throw new IllegalStateException("Category already exists");
            }

            existingCategory.setActive(true);
            existingCategory.setName(categoryName);

            Category reactivatedCategory = categoryRepository.save(existingCategory);
            log.info("Inactive category reactivated with id={}", reactivatedCategory.getId());

            return toDto(reactivatedCategory);
        }

        Category category = new Category();
        category.setName(categoryName);
        category.setActive(true);

        Category savedCategory = categoryRepository.save(category);

        log.info("Category created successfully with id={} name={}", savedCategory.getId(), savedCategory.getName());
        return toDto(savedCategory);
    }

    /**
     * Deactivates a category by ID.
     *
     * @param id the category ID
     * @return updated category DTO, or empty if not found
     */
    @Override
    public Optional<CategoryDto> deactivate(Long id) {
        log.info("Deactivating category with id={}", id);

        return categoryRepository.findById(id).map(category -> {
            category.setActive(false);
            Category savedCategory = categoryRepository.save(category);
            log.info("Category deactivated successfully with id={}", id);
            return toDto(savedCategory);
        }).or(() -> {
            log.warn("Category not found for deactivation. id={}", id);
            return Optional.empty();
        });
    }

    /**
     * Reactivates a category by ID.
     *
     * @param id the category ID
     * @return updated category DTO, or empty if not found
     */
    @Override
    public Optional<CategoryDto> reactivate(Long id) {
        log.info("Reactivating category with id={}", id);

        return categoryRepository.findById(id).map(category -> {
            category.setActive(true);
            Category savedCategory = categoryRepository.save(category);
            log.info("Category reactivated successfully with id={}", id);
            return toDto(savedCategory);
        }).or(() -> {
            log.warn("Category not found for reactivation. id={}", id);
            return Optional.empty();
        });
    }
}