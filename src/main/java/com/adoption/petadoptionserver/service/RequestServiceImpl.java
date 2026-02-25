package com.adoption.petadoptionserver.service;

import com.adoption.petadoptionserver.dto.AdoptionRequestDto;
import com.adoption.petadoptionserver.interfaces.RequestService;
import com.adoption.petadoptionserver.model.AdoptionRequest;
import com.adoption.petadoptionserver.model.Animal;
import com.adoption.petadoptionserver.model.User;
import com.adoption.petadoptionserver.repository.AdoptionRequestRepository;
import com.adoption.petadoptionserver.repository.AnimalRepository;
import com.adoption.petadoptionserver.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class RequestServiceImpl implements RequestService {

    private final AdoptionRequestRepository requestRepo;
    private final AnimalRepository animalRepo;
    private final UserRepository userRepo;

    private final DateTimeFormatter dtf = DateTimeFormatter.ISO_DATE_TIME;

    public RequestServiceImpl(AdoptionRequestRepository requestRepo,
                              AnimalRepository animalRepo,
                              UserRepository userRepo) {
        this.requestRepo = requestRepo;
        this.animalRepo = animalRepo;
        this.userRepo = userRepo;
    }

    private AdoptionRequestDto toDto(AdoptionRequest r) {
        return AdoptionRequestDto.builder()
                .id(r.getId())
                .animalId(r.getAnimal().getId())
                .userId(r.getUser().getId())
                .userName(r.getUser().getUsername())
                .message(r.getMessage())
                .status(r.getStatus())
                .reason(r.getReason())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().format(dtf) : null)
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().format(dtf) : null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdoptionRequestDto> find(Long userId, Long animalId) {
        List<AdoptionRequest> list;
        if (userId != null && animalId != null)
            list = requestRepo.findByUser_IdAndAnimal_Id(userId, animalId);
        else if (userId != null)
            list = requestRepo.findByUser_Id(userId);
        else if (animalId != null)
            list = requestRepo.findByAnimal_Id(animalId);
        else
            list = requestRepo.findAll();

        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdoptionRequestDto> findById(Long id) {
        return requestRepo.findById(id).map(this::toDto);
    }

    @Override
    public AdoptionRequestDto create(AdoptionRequestDto dto) {
        if (dto == null) throw new IllegalArgumentException("request dto is required");

        User user = userRepo.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Animal animal = animalRepo.findById(dto.getAnimalId())
                .orElseThrow(() -> new IllegalArgumentException("Animal not found"));

        // Prevent duplicate pending request
        if (requestRepo.existsByUser_IdAndAnimal_IdAndStatus(user.getId(), animal.getId(), "PENDING")) {
            throw new IllegalStateException("You already sent a pending request for this animal");
        }

        AdoptionRequest r = new AdoptionRequest();
        r.setUser(user);
        r.setAnimal(animal);
        r.setMessage(dto.getMessage());
        r.setStatus("PENDING");
        r.setReason(null);

        AdoptionRequest saved = requestRepo.save(r);
        return toDto(saved);
    }

    @Override
    public Optional<AdoptionRequestDto> approve(Long id) {
        return requestRepo.findById(id).map(r -> {
            if (!"PENDING".equals(r.getStatus())) {
                throw new IllegalStateException("Only pending requests can be approved");
            }

            r.setStatus("APPROVED");
            r.setReason(null);

            // OPTIONAL: change animal status to adopted (or whatever your domain requires)
            // Animal animal = r.getAnimal();
            // animal.setStatus("ADOPTED");
            // animalRepo.save(animal);

            AdoptionRequest saved = requestRepo.save(r);
            return toDto(saved);
        });
    }

    @Override
    public Optional<AdoptionRequestDto> reject(Long id, String reason) {
        return requestRepo.findById(id).map(r -> {
            if (!"PENDING".equals(r.getStatus())) {
                throw new IllegalStateException("Only pending requests can be rejected");
            }
            r.setStatus("REJECTED");
            r.setReason(reason);
            AdoptionRequest saved = requestRepo.save(r);
            return toDto(saved);
        });
    }

    @Override
    public boolean cancel(Long id) {
        AdoptionRequest r = requestRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (!"PENDING".equals(r.getStatus()))
            throw new IllegalStateException("Only pending requests can be canceled");

        r.setStatus("CANCELED");
        requestRepo.save(r);
        return true;
    }
}