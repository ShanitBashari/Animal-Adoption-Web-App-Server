package com.adoption.petadoptionserver.controller;

import com.adoption.petadoptionserver.dto.AnimalDto;
import com.adoption.petadoptionserver.dto.RejectRequest;
import com.adoption.petadoptionserver.dto.UserDto;
import com.adoption.petadoptionserver.interfaces.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for managing users and animals.
 * Provides administrative endpoints such as activating users,
 * deactivating users, listing animals, approving animals, and rejecting animals.
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Returns a list of all users in the system.
     *
     * @return list of users
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> listUsers() {
        return ResponseEntity.ok(adminService.listUsers());
    }

    /**
     * Deactivates a user by ID.
     *
     * @param id the user ID
     * @return updated user, or 404 if not found
     */
    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<UserDto> deactivate(@PathVariable Long id) {
        return adminService.deactivateUser(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Activates a user by ID.
     *
     * @param id the user ID
     * @return updated user, or 404 if not found
     */
    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<UserDto> activate(@PathVariable Long id) {
        return adminService.activateUser(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Removes an animal by ID.
     *
     * @param id the animal ID
     * @return 204 if deleted, or 404 if not found
     */
    @DeleteMapping("/animals/{id}")
    public ResponseEntity<Void> removeAnimal(@PathVariable Long id) {
        boolean removed = adminService.removeAnimal(id);
        return removed
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    /**
     * Returns a list of all animals for admin management.
     *
     * @return list of animals
     */
    @GetMapping("/animals")
    public ResponseEntity<List<AnimalDto>> listAnimals() {
        return ResponseEntity.ok(adminService.listAnimals());
    }

    /**
     * Approves an animal by ID.
     *
     * @param id the animal ID
     * @return the approved animal
     */
    @PutMapping("/animals/{id}/approve")
    public ResponseEntity<AnimalDto> approveAnimal(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.approveAnimal(id));
    }

    /**
     * Rejects an animal by ID with an optional rejection reason.
     *
     * @param id the animal ID
     * @param body optional request body containing the rejection reason
     * @return the rejected animal
     */
    @PutMapping("/animals/{id}/reject")
    public ResponseEntity<AnimalDto> rejectAnimal(
            @PathVariable Long id,
            @RequestBody(required = false) RejectRequest body
    ) {
        String reason = body != null ? body.getReason() : null;
        return ResponseEntity.ok(adminService.rejectAnimal(id, reason));
    }
}