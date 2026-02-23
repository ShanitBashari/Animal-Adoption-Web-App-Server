package com.adoption.petadoptionserver.controller;

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

    // List all users (admin)
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> listUsers() {
        return ResponseEntity.ok(adminService.listUsers());
    }

    // Promote user to admin
    @PostMapping("/users/{id}/promote")
    public ResponseEntity<UserDto> promote(@PathVariable Long id) {
        return adminService.promoteToAdmin(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Deactivate user
    @PostMapping("/users/{id}/deactivate")
    public ResponseEntity<UserDto> deactivate(@PathVariable Long id) {
        return adminService.deactivateUser(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Remove an animal (admin)
    @DeleteMapping("/animals/{id}")
    public ResponseEntity<Void> removeAnimal(@PathVariable Long id) {
        boolean ok = adminService.removeAnimal(id);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}