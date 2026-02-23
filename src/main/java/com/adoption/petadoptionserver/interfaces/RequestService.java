package com.adoption.petadoptionserver.interfaces;

// RequestService
import com.adoption.petadoptionserver.dto.AdoptionRequestDto;

import java.util.List;
import java.util.Optional;

public interface RequestService {
    List<AdoptionRequestDto> find(Long userId, Long animalId);
    Optional<AdoptionRequestDto> findById(Long id);
    AdoptionRequestDto create(AdoptionRequestDto dto);
    Optional<AdoptionRequestDto> approve(Long id);
    Optional<AdoptionRequestDto> reject(Long id, String reason);
    boolean cancel(Long id);
}