package com.adoption.petadoptionserver.controller;

import com.adoption.petadoptionserver.dto.AnimalDto;
import com.adoption.petadoptionserver.interfaces.AnimalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/animals")
@CrossOrigin(origins = "http://localhost:3000")
public class AnimalController {

    private final AnimalService animalService;
    private final ObjectMapper objectMapper;

    public AnimalController(AnimalService animalService, ObjectMapper objectMapper) {
        this.animalService = animalService;
        this.objectMapper = objectMapper;
    }

    // ✅ My Listings (requires auth)
    @GetMapping("/mine")
    public ResponseEntity<List<AnimalDto>> mine(Principal principal) {
        String username = principal.getName();
        return ResponseEntity.ok(animalService.findMine(username));
    }

    @GetMapping
    public ResponseEntity<List<AnimalDto>> getAll(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category
    ) {
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

    // ✅ create (requires auth)
    @PostMapping
    public ResponseEntity<AnimalDto> create(Principal principal, @Valid @RequestBody AnimalDto dto) {
        AnimalDto created = animalService.createForUser(principal.getName(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ✅ update (requires auth + owner)
    @PutMapping("/{id}")
    public ResponseEntity<AnimalDto> update(Principal principal, @PathVariable Long id, @Valid @RequestBody AnimalDto dto) {
        try {
            return animalService.updateForUser(principal.getName(), id, dto)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // ✅ delete (requires auth + owner)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Principal principal, @PathVariable Long id) {
        try {
            boolean deleted = animalService.deleteForUser(principal.getName(), id);
            return deleted ? ResponseEntity.noContent().build()
                    : ResponseEntity.notFound().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // ✅ create multipart (requires auth)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnimalDto> createMultipart(
            Principal principal,
            @RequestPart("body") String body,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {
            AnimalDto dto = objectMapper.readValue(body, AnimalDto.class);

            if (image != null && !image.isEmpty()) {
                String savedPath = saveUploadedFile(image);
                dto.setImage(savedPath);
            }

            AnimalDto created = animalService.createForUser(principal.getName(), dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ update multipart (requires auth + owner)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnimalDto> updateMultipart(
            Principal principal,
            @PathVariable Long id,
            @RequestPart("body") String body,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {
            AnimalDto dto = objectMapper.readValue(body, AnimalDto.class);

            if (image != null && !image.isEmpty()) {
                String savedPath = saveUploadedFile(image);
                dto.setImage(savedPath);
            }

            return animalService.updateForUser(principal.getName(), id, dto)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String saveUploadedFile(MultipartFile file) throws IOException {
        long maxBytes = 5 * 1024 * 1024;
        if (file.getSize() > maxBytes) throw new IOException("File too large");

        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
            if (ext.length() > 10) ext = "";
        }

        String filename = UUID.randomUUID() + ext;

        Path uploadsDir = Path.of("uploads");
        if (!Files.exists(uploadsDir)) Files.createDirectories(uploadsDir);

        Path target = uploadsDir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + filename;
    }
}