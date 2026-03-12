package com.adoption.petadoptionserver.controller;

import com.adoption.petadoptionserver.dto.CategoryDto;
import com.adoption.petadoptionserver.interfaces.CategoryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Admin controller for managing categories.
 * Provides endpoints for listing, creating, deactivating,
 * and reactivating categories.
 */
@RestController
@RequestMapping("/api/admin/categories")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminCategoryController {

    private static final Logger log = LoggerFactory.getLogger(AdminCategoryController.class);

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Returns all categories sorted alphabetically by name.
     * Sorting is case-insensitive.
     *
     * @return sorted list of categories
     */
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAll() {
        List<CategoryDto> categories = categoryService.findAll();
        List<CategoryDto> sortedCategories = sortCategoriesByName(categories);
        return ResponseEntity.ok(sortedCategories);
    }

    /**
     * Creates a new category.
     *
     * @param dto the category data
     * @param uriBuilder URI builder used to create the Location header
     * @return the created category with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<CategoryDto> create(
            @Valid @RequestBody CategoryDto dto,
            UriComponentsBuilder uriBuilder
    ) {
        log.info("Creating category with name={}", dto.getName());

        CategoryDto created = categoryService.create(dto);

        URI location = uriBuilder.path("/api/admin/categories/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    /**
     * Deactivates an existing category by ID.
     *
     * @param id the category ID
     * @return the updated category, or 404 if not found
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<CategoryDto> deactivate(@PathVariable Long id) {
        return categoryService.deactivate(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Reactivates an existing category by ID.
     *
     * @param id the category ID
     * @return the updated category, or 404 if not found
     */
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<CategoryDto> reactivate(@PathVariable Long id) {
        return categoryService.reactivate(id)
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