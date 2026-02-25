package com.adoption.petadoptionserver.interfaces;

import com.adoption.petadoptionserver.dto.CategoryDto;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<CategoryDto> findAll();
    Optional<CategoryDto> findById(Long id);
    Optional<CategoryDto> findByName(String name);
    CategoryDto create(CategoryDto dto);
    Optional<CategoryDto> update(Long id, CategoryDto dto);
    boolean delete(Long id);
}