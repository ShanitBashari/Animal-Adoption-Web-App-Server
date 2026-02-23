package com.adoption.petadoptionserver.service;

// MockRequestService
import com.adoption.petadoptionserver.dto.AdoptionRequestDto;
import com.adoption.petadoptionserver.interfaces.RequestService;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@Profile("mock")
public class MockRequestService implements RequestService {

    private final Map<Long, AdoptionRequestDto> store = new LinkedHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    @PostConstruct
    public void seed() {
        // no seed by default
    }

    @Override
    public List<AdoptionRequestDto> find(Long userId, Long animalId) {
        return store.values().stream()
                .filter(r -> (userId == null || Objects.equals(r.getUserId(), userId)) &&
                        (animalId == null || Objects.equals(r.getAnimalId(), animalId)))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AdoptionRequestDto> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public AdoptionRequestDto create(AdoptionRequestDto dto) {
        Long id = idGen.getAndIncrement();
        dto.setId(id);
        dto.setStatus(dto.getStatus() == null ? "NEW" : dto.getStatus());
        String now = Instant.now().toString();
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now);
        store.put(id, dto);
        return dto;
    }

    @Override
    public Optional<AdoptionRequestDto> approve(Long id) {
        AdoptionRequestDto r = store.get(id);
        if (r == null) return Optional.empty();
        r.setStatus("APPROVED");
        r.setUpdatedAt(Instant.now().toString());
        return Optional.of(r);
    }

    @Override
    public Optional<AdoptionRequestDto> reject(Long id, String reason) {
        AdoptionRequestDto r = store.get(id);
        if (r == null) return Optional.empty();
        r.setStatus("REJECTED");
        r.setReason(reason);
        r.setUpdatedAt(Instant.now().toString());
        return Optional.of(r);
    }

    @Override
    public boolean cancel(Long id) {
        AdoptionRequestDto r = store.get(id);
        if (r == null) return false;
        r.setStatus("CANCELLED");
        r.setUpdatedAt(Instant.now().toString());
        return true;
    }
}