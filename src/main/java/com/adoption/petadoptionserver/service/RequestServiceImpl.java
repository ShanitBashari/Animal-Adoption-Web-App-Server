package com.adoption.petadoptionserver.service;

import com.adoption.petadoptionserver.dto.AdoptionRequestDto;
import com.adoption.petadoptionserver.enums.AdoptionRequestStatus;
import com.adoption.petadoptionserver.enums.AnimalStatus;
import com.adoption.petadoptionserver.interfaces.RequestService;
import com.adoption.petadoptionserver.model.AdoptionRequest;
import com.adoption.petadoptionserver.model.Animal;
import com.adoption.petadoptionserver.model.User;
import com.adoption.petadoptionserver.repository.AdoptionRequestRepository;
import com.adoption.petadoptionserver.repository.AnimalRepository;
import com.adoption.petadoptionserver.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for adoption request management.
 * Handles creation, retrieval, approval, rejection, and cancellation of requests.
 */
@Service
@Transactional
public class RequestServiceImpl implements RequestService {

    private static final Logger log = LoggerFactory.getLogger(RequestServiceImpl.class);

    private static final String STATUS_PENDING = String.valueOf(AdoptionRequestStatus.PENDING);
    private static final String STATUS_APPROVED = String.valueOf(AdoptionRequestStatus.APPROVED);
    private static final String STATUS_REJECTED = String.valueOf(AdoptionRequestStatus.REJECTED);
    private static final String STATUS_CANCELED = String.valueOf(AdoptionRequestStatus.CANCELED);

    private final AdoptionRequestRepository requestRepository;
    private final AnimalRepository animalRepository;
    private final UserRepository userRepository;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;

    public RequestServiceImpl(
            AdoptionRequestRepository requestRepository,
            AnimalRepository animalRepository,
            UserRepository userRepository
    ) {
        this.requestRepository = requestRepository;
        this.animalRepository = animalRepository;
        this.userRepository = userRepository;
    }

    /**
     * Converts an AdoptionRequest entity into an AdoptionRequestDto.
     *
     * @param request the adoption request entity
     * @return mapped adoption request DTO
     */
    private AdoptionRequestDto toDto(AdoptionRequest request) {
        AdoptionRequestDto dto = new AdoptionRequestDto();
        dto.setId(request.getId());
        dto.setAnimalId(request.getAnimal().getId());
        dto.setUserId(request.getUser().getId());
        dto.setUserName(request.getUser().getUsername());
        dto.setMessage(request.getMessage());
        dto.setStatus(request.getStatus());
        dto.setReason(request.getReason());
        dto.setCreatedAt(request.getCreatedAt() != null ? request.getCreatedAt().format(dateTimeFormatter) : null);
        dto.setUpdatedAt(request.getUpdatedAt() != null ? request.getUpdatedAt().format(dateTimeFormatter) : null);
        dto.setAnimalName(request.getAnimal().getName());
        dto.setAnimalImage(request.getAnimal().getImage());
        dto.setAnimalCategory(request.getAnimal().getCategory() != null ? request.getAnimal().getCategory().getName() : null);
        dto.setAnimalLocation(request.getAnimal().getLocation());
        dto.setAnimalGender(request.getAnimal().getGender());
        dto.setAnimalAge(request.getAnimal().getAge());
        dto.setAnimalSize(request.getAnimal().getSize());
        dto.setAnimalOwnerName(request.getAnimal().getOwnerName());
        dto.setAnimalOwnerPhone(request.getAnimal().getOwnerPhone());
        dto.setAnimalDescription(request.getAnimal().getDescription());
        dto.setAnimalStatus(request.getAnimal().getStatus());

        return dto;
    }

    /**
     * Returns adoption requests optionally filtered by user ID and/or animal ID.
     *
     * @param userId optional user ID filter
     * @param animalId optional animal ID filter
     * @return list of adoption request DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public List<AdoptionRequestDto> find(Long userId, Long animalId) {
        List<AdoptionRequest> requests;

        if (userId != null && animalId != null) {
            requests = requestRepository.findByUser_IdAndAnimal_Id(userId, animalId);
        } else if (userId != null) {
            requests = requestRepository.findByUser_Id(userId);
        } else if (animalId != null) {
            requests = requestRepository.findByAnimal_Id(animalId);
        } else {
            requests = requestRepository.findAll();
        }

        return requests.stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Returns an adoption request by ID.
     *
     * @param id the request ID
     * @return optional adoption request DTO
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<AdoptionRequestDto> findById(Long id) {
        return requestRepository.findById(id).map(this::toDto);
    }

    /**
     * Creates a new adoption request after validating business constraints.
     *
     * @param dto the adoption request data
     * @return created adoption request DTO
     */
    @Override
    public AdoptionRequestDto create(AdoptionRequestDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("request dto is required");
        }

        log.info("Creating adoption request for userId={} animalId={}", dto.getUserId(), dto.getAnimalId());

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> {
                    log.warn("User not found for adoption request. userId={}", dto.getUserId());
                    return new IllegalArgumentException("User not found");
                });

        Animal animal = animalRepository.findById(dto.getAnimalId())
                .orElseThrow(() -> {
                    log.warn("Animal not found for adoption request. animalId={}", dto.getAnimalId());
                    return new IllegalArgumentException("Animal not found");
                });

        if (animal.getOwnerUser() != null && animal.getOwnerUser().getId().equals(user.getId())) {
            log.warn("User cannot adopt own animal. userId={} animalId={}", user.getId(), animal.getId());
            throw new IllegalStateException("You cannot adopt your own animal listing");
        }

        if (requestRepository.existsByUser_IdAndAnimal_IdAndStatus(user.getId(), animal.getId(), STATUS_PENDING)) {
            log.warn("Pending adoption request already exists. userId={} animalId={}", user.getId(), animal.getId());
            throw new IllegalStateException("You already sent a pending request for this animal");
        }

        AdoptionRequest request = new AdoptionRequest();
        request.setUser(user);
        request.setAnimal(animal);
        request.setMessage(dto.getMessage());
        request.setStatus(STATUS_PENDING);
        request.setReason(null);

        AdoptionRequest savedRequest = requestRepository.save(request);

        log.info("Adoption request created successfully with id={}", savedRequest.getId());
        return toDto(savedRequest);
    }

    /**
     * Approves an existing pending adoption request.
     *
     * @param id the request ID
     * @return updated adoption request DTO, or empty if not found
     */
    @Override
    public Optional<AdoptionRequestDto> approve(Long id) {
        log.info("Approving adoption request with id={}", id);

        return requestRepository.findById(id).map(request -> {
            if (!STATUS_PENDING.equals(request.getStatus())) {
                log.warn("Only pending requests can be approved. id={} status={}", id, request.getStatus());
                throw new IllegalStateException("Only pending requests can be approved");
            }

            request.setStatus(STATUS_APPROVED);
            request.setReason(null);

            AdoptionRequest savedRequest = requestRepository.save(request);
            log.info("Adoption request approved successfully with id={}", id);

            return toDto(savedRequest);
        }).or(() -> {
            log.warn("Adoption request not found for approval. id={}", id);
            return Optional.empty();
        });
    }

    /**
     * Rejects an existing pending adoption request.
     *
     * @param id the request ID
     * @param reason optional rejection reason
     * @return updated adoption request DTO, or empty if not found
     */
    @Override
    public Optional<AdoptionRequestDto> reject(Long id, String reason) {
        log.info("Rejecting adoption request with id={}", id);

        return requestRepository.findById(id).map(request -> {
            if (!STATUS_PENDING.equals(request.getStatus())) {
                log.warn("Only pending requests can be rejected. id={} status={}", id, request.getStatus());
                throw new IllegalStateException("Only pending requests can be rejected");
            }

            request.setStatus(STATUS_REJECTED);
            request.setReason(reason);

            AdoptionRequest savedRequest = requestRepository.save(request);
            log.info("Adoption request rejected successfully with id={}", id);

            return toDto(savedRequest);
        }).or(() -> {
            log.warn("Adoption request not found for rejection. id={}", id);
            return Optional.empty();
        });
    }

    /**
     * Cancels an existing pending adoption request.
     *
     * @param id the request ID
     * @return true if canceled successfully
     */
    @Override
    public boolean cancel(Long id) {
        log.info("Canceling adoption request with id={}", id);

        AdoptionRequest request = requestRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Adoption request not found for cancellation. id={}", id);
                    return new IllegalArgumentException("Request not found");
                });

        if (!STATUS_PENDING.equals(request.getStatus())) {
            log.warn("Only pending requests can be canceled. id={} status={}", id, request.getStatus());
            throw new IllegalStateException("Only pending requests can be canceled");
        }

        request.setStatus(STATUS_CANCELED);
        requestRepository.save(request);

        log.info("Adoption request canceled successfully with id={}", id);
        return true;
    }
}