package com.adoption.petadoptionserver.controller;

import com.adoption.petadoptionserver.dto.AnimalDto;
import com.adoption.petadoptionserver.dto.RejectRequest;
import com.adoption.petadoptionserver.dto.UserDto;
import com.adoption.petadoptionserver.interfaces.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> listUsers() {
        return ResponseEntity.ok(adminService.listUsers());
    }

    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<UserDto> deactivate(@PathVariable Long id) {
        return adminService.deactivateUser(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<UserDto> activate(@PathVariable Long id) {
        return adminService.activateUser(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/animals/{id}")
    public ResponseEntity<Void> removeAnimal(@PathVariable Long id) {
        boolean ok = adminService.removeAnimal(id);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/animals")
    public ResponseEntity<List<AnimalDto>> listAnimals() {
        return ResponseEntity.ok(adminService.listAnimals());
    }

    @PutMapping("/animals/{id}/approve")
    public ResponseEntity<AnimalDto> approveAnimal(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.approveAnimal(id));
    }

    @PutMapping("/animals/{id}/reject")
    public ResponseEntity<AnimalDto> rejectAnimal(
            @PathVariable Long id,
            @RequestBody(required = false) RejectRequest body) {

        String reason = body != null ? body.getReason() : null;
        return ResponseEntity.ok(adminService.rejectAnimal(id, reason));
    }
}