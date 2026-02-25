package com.adoption.petadoptionserver.controller;

import com.adoption.petadoptionserver.dto.AnimalDto;
import com.adoption.petadoptionserver.interfaces.AnimalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/animals")
@CrossOrigin(origins = "http://localhost:3000")
public class AnimalController {

    private final AnimalService animalService;

    public AnimalController(AnimalService animalService) {
        this.animalService = animalService;
    }

    @GetMapping
    public ResponseEntity<List<AnimalDto>> getAll(@RequestParam(required = false) String q, @RequestParam(required = false) String category) {
        if ((q == null || q.isBlank()) && (category == null || category.isBlank())) {
            return ResponseEntity.ok(animalService.findAll());
        }
        return ResponseEntity.ok(animalService.search(q, category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimalDto> getById(@PathVariable Long id) {
        return animalService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AnimalDto> create(@Valid @RequestBody AnimalDto dto) {
        AnimalDto created = animalService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnimalDto> update(@PathVariable Long id, @Valid @RequestBody AnimalDto dto) {
        return animalService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = animalService.delete(id);
        return deleted ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}