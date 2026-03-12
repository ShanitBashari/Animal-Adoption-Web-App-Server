package com.adoption.petadoptionserver.controller;

import com.adoption.petadoptionserver.dto.CategoryDto;
import com.adoption.petadoptionserver.interfaces.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Public controller for category-related operations.
 * Provides endpoints for listing active categories and retrieving a category by ID.
 */
@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "http://localhost:3000")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Returns all active categories sorted alphabetically by name.
     *
     * @return sorted list of active categories
     */
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllActive() {
        List<CategoryDto> categories = categoryService.findAllActive();
        List<CategoryDto> sortedCategories = sortCategoriesByName(categories);
        return ResponseEntity.ok(sortedCategories);
    }

    /**
     * Returns a category by ID.
     *
     * @param id the category ID
     * @return the category, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getOne(@PathVariable Long id) {
        return categoryService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Sorts categories alphabetically by name using a case-insensitive comparator.
     *
     * @param categories the list of categories to sort
     * @return sorted list of categories
     */
    private List<CategoryDto> sortCategoriesByName(List<CategoryDto> categories) {
        Collator collator = Collator.getInstance(Locale.ENGLISH);
        collator.setStrength(Collator.PRIMARY);

        Comparator<CategoryDto> comparator = Comparator.comparing(
                category -> category.getName() == null ? "" : category.getName(),
                collator
        );

        return categories.stream()
                .sorted(comparator)
                .toList();
    }
}