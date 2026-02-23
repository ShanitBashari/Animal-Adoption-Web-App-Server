package com.adoption.petadoptionserver.interfaces;

import com.adoption.petadoptionserver.dto.AnimalDto;

import java.util.List;
import java.util.Optional;

public interface AnimalService {
    List<AnimalDto> findAll();
    List<AnimalDto> search(String q, String category);
    Optional<AnimalDto> findById(Long id);
    AnimalDto create(AnimalDto dto);
    Optional<AnimalDto> update(Long id, AnimalDto dto);
    boolean delete(Long id);
}