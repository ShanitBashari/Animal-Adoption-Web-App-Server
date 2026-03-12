package com.adoption.petadoptionserver.controller;

import com.adoption.petadoptionserver.dto.UserDto;
import com.adoption.petadoptionserver.interfaces.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Controller for user-related operations.
 * Provides endpoints for listing users, retrieving users,
 * creating users, and deleting users.
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Returns all users in the system.
     *
     * @return list of users
     */
    @GetMapping
    public ResponseEntity<List<UserDto>> getAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    /**
     * Returns a user by ID.
     *
     * @param id the user ID
     * @return the user, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new user.
     *
     * @param dto the user data
     * @param uriBuilder URI builder used to create the Location header
     * @return the created user with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<UserDto> create(
            @Valid @RequestBody UserDto dto,
            UriComponentsBuilder uriBuilder
    ) {
        UserDto created = userService.createUser(dto);
        URI location = uriBuilder.path("/api/users/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    /**
     * Deletes a user by ID.
     *
     * @param id the user ID
     * @return 204 if deleted, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}