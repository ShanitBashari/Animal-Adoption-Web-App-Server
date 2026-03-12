package com.adoption.petadoptionserver.controller;

import com.adoption.petadoptionserver.dto.AnimalDto;
import com.adoption.petadoptionserver.interfaces.AnimalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

/**
 * Controller for animal-related operations.
 * Supports public browsing and authenticated user actions
 * such as creating, updating, deleting, and activating listings.
 */
@RestController
@RequestMapping("/api/animals")
@CrossOrigin(origins = "http://localhost:3000")
public class AnimalController {

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;
    private static final int MAX_EXTENSION_LENGTH = 10;
    private static final String UPLOADS_DIRECTORY = "uploads";
    private static final String UPLOADS_URL_PREFIX = "/uploads/";

    private final AnimalService animalService;
    private final ObjectMapper objectMapper;

    public AnimalController(AnimalService animalService, ObjectMapper objectMapper) {
        this.animalService = animalService;
        this.objectMapper = objectMapper;
    }

    /**
     * Returns all animals created by the currently authenticated user.
     *
     * @param principal the authenticated user
     * @return list of the user's animals
     */
    @GetMapping("/mine")
    public ResponseEntity<List<AnimalDto>> getMine(Principal principal) {
        return ResponseEntity.ok(animalService.findMine(principal.getName()));
    }

    /**
     * Returns all animals, optionally filtered by search query and category.
     *
     * @param q optional free-text search query
     * @param category optional category filter
     * @return list of matching animals
     */
    @GetMapping
    public ResponseEntity<List<AnimalDto>> getAll(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category
    ) {
        if (isBlank(q) && isBlank(category)) {
            return ResponseEntity.ok(animalService.findAll());
        }

        return ResponseEntity.ok(animalService.search(q, category));
    }

    /**
     * Returns a single animal by ID.
     *
     * @param id the animal ID
     * @return the animal, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<AnimalDto> getById(@PathVariable Long id) {
        return animalService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new animal listing for the authenticated user.
     *
     * @param principal the authenticated user
     * @param dto the animal data
     * @return the created animal with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<AnimalDto> create(
            Principal principal,
            @Valid @RequestBody AnimalDto dto
    ) {
        AnimalDto created = animalService.createForUser(principal.getName(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Updates an existing animal listing owned by the authenticated user.
     *
     * @param principal the authenticated user
     * @param id the animal ID
     * @param dto the updated animal data
     * @return updated animal, 404 if not found, or 403 if forbidden
     */
    @PutMapping("/{id}")
    public ResponseEntity<AnimalDto> update(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody AnimalDto dto
    ) {
        try {
            return animalService.updateForUser(principal.getName(), id, dto)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Deletes an animal listing owned by the authenticated user.
     *
     * @param principal the authenticated user
     * @param id the animal ID
     * @return 204 if deleted, 404 if not found, or 403 if forbidden
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Principal principal, @PathVariable Long id) {
        try {
            boolean deleted = animalService.deleteForUser(principal.getName(), id);
            return deleted
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.notFound().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Creates a new animal listing using multipart form data.
     * The "body" part contains the animal JSON and the "image" part contains an optional image file.
     *
     * @param principal the authenticated user
     * @param body the animal JSON as string
     * @param image optional uploaded image
     * @return the created animal, or an error response if the request is invalid
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnimalDto> createMultipart(
            Principal principal,
            @RequestPart("body") String body,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {
            AnimalDto dto = objectMapper.readValue(body, AnimalDto.class);

            if (image != null && !image.isEmpty()) {
                dto.setImage(saveUploadedFile(image));
            }

            AnimalDto created = animalService.createForUser(principal.getName(), dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);

        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Updates an animal listing using multipart form data.
     * The "body" part contains the animal JSON and the "image" part contains an optional image file.
     *
     * @param principal the authenticated user
     * @param id the animal ID
     * @param body the animal JSON as string
     * @param image optional uploaded image
     * @return the updated animal, or an appropriate error response
     */
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
                dto.setImage(saveUploadedFile(image));
            }

            return animalService.updateForUser(principal.getName(), id, dto)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());

        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Reactivates an animal listing owned by the authenticated user.
     *
     * @param principal the authenticated user
     * @param id the animal ID
     * @return updated animal, 404 if not found, or 403 if forbidden
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<AnimalDto> activate(Principal principal, @PathVariable Long id) {
        try {
            return animalService.activateForUser(principal.getName(), id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Saves an uploaded file to the local uploads directory and returns its public path.
     *
     * @param file the uploaded file
     * @return relative public path of the saved file
     * @throws IOException if the file is invalid or cannot be saved
     */
    private String saveUploadedFile(MultipartFile file) throws IOException {
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IOException("File too large");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            if (extension.length() > MAX_EXTENSION_LENGTH) {
                extension = "";
            }
        }

        String filename = UUID.randomUUID() + extension;

        Path uploadsDir = Path.of(UPLOADS_DIRECTORY);
        if (!Files.exists(uploadsDir)) {
            Files.createDirectories(uploadsDir);
        }

        Path target = uploadsDir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return UPLOADS_URL_PREFIX + filename;
    }

    /**
     * Checks whether a string is null, empty, or blank.
     *
     * @param value the string to check
     * @return true if the string is blank, otherwise false
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}