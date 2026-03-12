package com.adoption.petadoptionserver.controller;

import com.adoption.petadoptionserver.dto.AdoptionRequestDto;
import com.adoption.petadoptionserver.interfaces.RequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing adoption requests.
 * Supports creating, retrieving, listing, approving,
 * rejecting, and canceling adoption requests.
 */
@RestController
@RequestMapping("/api/requests")
@CrossOrigin(origins = "http://localhost:3000")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    /**
     * Creates a new adoption request.
     *
     * @param dto the adoption request data
     * @param uriBuilder URI builder used to create the Location header
     * @return the created adoption request with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<AdoptionRequestDto> create(
            @Valid @RequestBody AdoptionRequestDto dto,
            UriComponentsBuilder uriBuilder
    ) {
        AdoptionRequestDto created = requestService.create(dto);
        URI location = uriBuilder.path("/api/requests/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    /**
     * Returns an adoption request by ID.
     *
     * @param id the request ID
     * @return the adoption request, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdoptionRequestDto> getById(@PathVariable Long id) {
        return requestService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Returns adoption requests optionally filtered by user ID or animal ID.
     *
     * @param userId optional user ID filter
     * @param animalId optional animal ID filter
     * @return list of matching adoption requests
     */
    @GetMapping
    public ResponseEntity<List<AdoptionRequestDto>> list(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "animalId", required = false) Long animalId
    ) {
        return ResponseEntity.ok(requestService.find(userId, animalId));
    }

    /**
     * Approves an adoption request by ID.
     *
     * @param id the request ID
     * @return the approved adoption request, or 404 if not found
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<AdoptionRequestDto> approve(@PathVariable Long id) {
        return requestService.approve(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Rejects an adoption request by ID with an optional rejection reason.
     *
     * @param id the request ID
     * @param body optional request body containing the rejection reason
     * @return the rejected adoption request, or 404 if not found
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<AdoptionRequestDto> reject(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String reason = body != null ? body.get("reason") : null;
        return requestService.reject(id, reason)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Cancels an adoption request by ID.
     *
     * @param id the request ID
     * @return 204 if canceled, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        boolean canceled = requestService.cancel(id);
        return canceled
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}