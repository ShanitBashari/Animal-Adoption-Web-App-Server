package com.adoption.petadoptionserver.controller;

import com.adoption.petadoptionserver.dto.AnimalDto;
import com.adoption.petadoptionserver.interfaces.AnimalService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
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
    public ResponseEntity<List<AnimalDto>> getAll() {
        List<AnimalDto> list = animalService.findAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimalDto> getById(@PathVariable Long id) {
        return animalService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AnimalDto> create(@RequestBody AnimalDto dto, UriComponentsBuilder uriBuilder) {
        AnimalDto created = animalService.create(dto);
        URI uri = uriBuilder.path("/api/animals/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(uri).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnimalDto> update(@PathVariable Long id, @RequestBody AnimalDto dto) {
        return animalService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = animalService.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // Optional: search/filter endpoints
    @GetMapping("/search")
    public ResponseEntity<List<AnimalDto>> search(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "category", required = false) String category) {
        List<AnimalDto> results = animalService.search(q, category);
        return ResponseEntity.ok(results);
    }
}