package com.adoption.petadoptionserver.controller;

import com.adoption.petadoptionserver.dto.UserDto;
import com.adoption.petadoptionserver.interfaces.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Get all users (admin)
    @GetMapping
    public ResponseEntity<List<UserDto>> getAll() {
        List<UserDto> list = userService.findAll();
        return ResponseEntity.ok(list);
    }

    // Get current user or by id
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Create user (could be used by admin or registration endpoint)
    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody UserDto dto, UriComponentsBuilder uriBuilder) {
        UserDto created = userService.create(dto);
        URI uri = uriBuilder.path("/api/users/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(uri).body(created);
    }

    // Update profile
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> update(@PathVariable Long id, @RequestBody UserDto dto) {
        return userService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = userService.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}