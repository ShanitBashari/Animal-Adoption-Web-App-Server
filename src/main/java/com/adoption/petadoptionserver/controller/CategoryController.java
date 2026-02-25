package com.adoption.petadoptionserver.controller;

import com.adoption.petadoptionserver.dto.CategoryDto;
import com.adoption.petadoptionserver.interfaces.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "http://localhost:3000")
public class CategoryController {

    private final CategoryService svc;

    public CategoryController(CategoryService svc) {
        this.svc = svc;
    }

    // GET all — used by frontend to populate dropdown
    @GetMapping
    public ResponseEntity<List<CategoryDto>> all() {
        return ResponseEntity.ok(svc.findAll());
    }

    // GET one by id
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getOne(@PathVariable Long id) {
        return svc.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // create (admin)
    @PostMapping
    public ResponseEntity<CategoryDto> create(@Valid @RequestBody CategoryDto dto, UriComponentsBuilder uriBuilder) {
        CategoryDto created = svc.create(dto);
        URI uri = uriBuilder.path("/api/categories/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(uri).body(created);
    }

    // update (admin)
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> update(@PathVariable Long id, @Valid @RequestBody CategoryDto dto) {
        return svc.update(id, dto).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // delete (admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = svc.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}