package com.adoption.petadoptionserver.controller;

import com.adoption.petadoptionserver.dto.AdoptionRequestDto;
import com.adoption.petadoptionserver.interfaces.RequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
@CrossOrigin(origins = "http://localhost:3000")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    // Create a new adoption request
    @PostMapping
    public ResponseEntity<AdoptionRequestDto> create(@RequestBody AdoptionRequestDto dto, UriComponentsBuilder uriBuilder) {
        AdoptionRequestDto created = requestService.create(dto);
        URI uri = uriBuilder.path("/api/requests/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(uri).body(created);
    }

    // Get request by id
    @GetMapping("/{id}")
    public ResponseEntity<AdoptionRequestDto> getById(@PathVariable Long id) {
        return requestService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // List all requests (admin) or for a user (filter by userId)
    @GetMapping
    public ResponseEntity<List<AdoptionRequestDto>> list(@RequestParam(value = "userId", required = false) Long userId,
                                                         @RequestParam(value = "animalId", required = false) Long animalId) {
        List<AdoptionRequestDto> list = requestService.find(userId, animalId);
        return ResponseEntity.ok(list);
    }

    // Approve a request
    @PostMapping("/{id}/approve")
    public ResponseEntity<AdoptionRequestDto> approve(@PathVariable Long id) {
        return requestService.approve(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Reject a request
    @PostMapping("/{id}/reject")
    public ResponseEntity<AdoptionRequestDto> reject(@PathVariable Long id, @RequestBody(required = false) String reason) {
        return requestService.reject(id, reason)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Cancel (by requester)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        boolean ok = requestService.cancel(id);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
