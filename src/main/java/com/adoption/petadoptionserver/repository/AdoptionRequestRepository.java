package com.adoption.petadoptionserver.repository;

import com.adoption.petadoptionserver.model.AdoptionRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdoptionRequestRepository extends JpaRepository<AdoptionRequest, Long> {

    List<AdoptionRequest> findByUser_Id(Long userId);
    List<AdoptionRequest> findByAnimal_Id(Long animalId);
    List<AdoptionRequest> findByUser_IdAndAnimal_Id(Long userId, Long animalId);
    List<AdoptionRequest> findByUser_IdOrderByCreatedAtDesc(Long userId);
    boolean existsByUser_IdAndAnimal_IdAndStatus(Long userId, Long animalId, String status);
    boolean existsByAnimal_Id(Long animalId);
}