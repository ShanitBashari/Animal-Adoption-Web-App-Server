package com.adoption.petadoptionserver.interfaces;

import com.adoption.petadoptionserver.dto.AdoptionRequestDto;

import java.util.List;
import java.util.Optional;

public interface RequestService {

    List<AdoptionRequestDto> find(Long userId, Long animalId);
    Optional<AdoptionRequestDto> findById(Long id);
    List<AdoptionRequestDto> findMine(String username);
    List<AdoptionRequestDto> findReceived(String username);
    AdoptionRequestDto create(String username, AdoptionRequestDto dto);
    Optional<AdoptionRequestDto> approve(String username, Long id);
    Optional<AdoptionRequestDto> reject(String username, Long id, String reason);
    boolean cancel(String username, Long id);
}
