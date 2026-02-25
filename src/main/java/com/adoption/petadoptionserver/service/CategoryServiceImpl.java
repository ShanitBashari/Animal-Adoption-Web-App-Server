package com.adoption.petadoptionserver.service;

import com.adoption.petadoptionserver.dto.CategoryDto;
import com.adoption.petadoptionserver.interfaces.CategoryService;
import com.adoption.petadoptionserver.model.Category;
import com.adoption.petadoptionserver.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repo;

    public CategoryServiceImpl(CategoryRepository repo) {
        this.repo = repo;
    }

    private CategoryDto toDto(Category c) {
        if (c == null) return null;
        return new CategoryDto(c.getId(), c.getName());
    }

    private Category toEntity(CategoryDto dto) {
        if (dto == null) return null;
        Category c = new Category();
        c.setName(dto.getName() != null ? dto.getName().trim() : null);
        return c;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> findAll() {
        return repo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDto> findById(Long id) {
        return repo.findById(id).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDto> findByName(String name) {
        if (name == null) return Optional.empty();
        return repo.findByNameIgnoreCase(name.trim()).map(this::toDto);
    }

    @Override
    public CategoryDto create(CategoryDto dto) {
        if (dto == null || dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("category name is required");
        }

        String name = dto.getName().trim();

        // בדיקת קיום (case-insensitive)
        if (repo.existsByNameIgnoreCase(name)) {
            throw new IllegalStateException("Category already exists");
        }

        Category saved = repo.save(toEntity(dto));
        return toDto(saved);
    }

    @Override
    public Optional<CategoryDto> update(Long id, CategoryDto dto) {
        if (dto == null || dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("category name is required");
        }

        return repo.findById(id).map(existing -> {
            String newName = dto.getName().trim();

            // אם יש קטגוריה אחרת עם אותו שם (case-insensitive) — להחזיר שגיאה
            repo.findByNameIgnoreCase(newName).ifPresent(conflict -> {
                if (!conflict.getId().equals(id)) {
                    throw new IllegalStateException("Category name already in use");
                }
            });

            existing.setName(newName);
            Category saved = repo.save(existing);
            return toDto(saved);
        });
    }

    @Override
    public boolean delete(Long id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }
}