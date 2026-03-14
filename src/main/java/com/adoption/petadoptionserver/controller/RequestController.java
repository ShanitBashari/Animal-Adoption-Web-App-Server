package com.adoption.petadoptionserver.controller;

import com.adoption.petadoptionserver.dto.AdoptionRequestDto;
import com.adoption.petadoptionserver.interfaces.RequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
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
     * Creates a new adoption request for the authenticated user.
     *
     * @param principal the authenticated user
     * @param dto the adoption request data
     * @param uriBuilder URI builder used to create the Location header
     * @return the created adoption request with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<AdoptionRequestDto> create(
            Principal principal,
            @Valid @RequestBody AdoptionRequestDto dto,
            UriComponentsBuilder uriBuilder
    ) {
        AdoptionRequestDto created = requestService.create(principal.getName(), dto);
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
     * Returns adoption requests created by the authenticated user.
     *
     * @param principal the authenticated user
     * @return list of sent adoption requests
     */
    @GetMapping("/mine")
    public ResponseEntity<List<AdoptionRequestDto>> mine(Principal principal) {
        return ResponseEntity.ok(requestService.findMine(principal.getName()));
    }

    /**
     * Returns adoption requests received on animals owned by the authenticated user.
     *
     * @param principal the authenticated user
     * @return list of received adoption requests
     */
    @GetMapping("/received")
    public ResponseEntity<List<AdoptionRequestDto>> received(Principal principal) {
        return ResponseEntity.ok(requestService.findReceived(principal.getName()));
    }

    /**
     * Returns adoption requests optionally filtered by user ID or animal ID.
     * Keep this only if you still need it internally/admin-wise.
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
     * Only the owner of the animal can approve.
     *
     * @param principal the authenticated user
     * @param id the request ID
     * @return the approved adoption request, 404 if not found, or 403 if forbidden
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<AdoptionRequestDto> approve(Principal principal, @PathVariable Long id) {
        try {
            return requestService.approve(principal.getName(), id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Rejects an adoption request by ID with an optional rejection reason.
     * Only the owner of the animal can reject.
     *
     * @param principal the authenticated user
     * @param id the request ID
     * @param body optional request body containing the rejection reason
     * @return the rejected adoption request, 404 if not found, or 403 if forbidden
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<AdoptionRequestDto> reject(
            Principal principal,
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body
    ) {
        try {
            String reason = body != null ? body.get("reason") : null;
            return requestService.reject(principal.getName(), id, reason)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Cancels an adoption request by ID.
     * Only the user who created the request can cancel it.
     *
     * @param principal the authenticated user
     * @param id the request ID
     * @return 204 if canceled, 404 if not found, or 403 if forbidden
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(Principal principal, @PathVariable Long id) {
        try {
            boolean canceled = requestService.cancel(principal.getName(), id);
            return canceled
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
