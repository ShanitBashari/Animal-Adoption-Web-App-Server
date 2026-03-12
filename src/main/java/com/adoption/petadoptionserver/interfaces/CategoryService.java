package com.adoption.petadoptionserver.interfaces;

import com.adoption.petadoptionserver.dto.CategoryDto;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<CategoryDto> findAll();
    List<CategoryDto> findAllActive();
    Optional<CategoryDto> findById(Long id);
    Optional<CategoryDto> findByName(String name);
    CategoryDto create(CategoryDto dto);
    Optional<CategoryDto> deactivate(Long id);
    Optional<CategoryDto> reactivate(Long id);
}