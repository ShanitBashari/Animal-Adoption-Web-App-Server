package com.adoption.petadoptionserver.controller;

import com.adoption.petadoptionserver.dto.CategoryDto;
import com.adoption.petadoptionserver.interfaces.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
        List<CategoryDto> list = svc.findAll();
        if (list == null || list.isEmpty()) {
            return ResponseEntity.ok(list);
        }

        Collator collator = Collator.getInstance(new Locale("he"));
        collator.setStrength(Collator.PRIMARY);
        Comparator<CategoryDto> cmp = Comparator.comparing(
                c -> (c.getName() == null ? "" : c.getName()),
                collator
        );

        List<CategoryDto> sorted = list.stream()
                .sorted(cmp)
                .collect(Collectors.toList());

        return ResponseEntity.ok(sorted);
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

    // delete (admin)   ;
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = svc.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}