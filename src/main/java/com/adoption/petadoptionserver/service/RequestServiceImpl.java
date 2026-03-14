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

    private User mustFindUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found for username={}", username);
                    return new IllegalArgumentException("User not found: " + username);
                });
    }

    private void mustBeAnimalOwner(String username, AdoptionRequest request) {
        User currentUser = mustFindUser(username);
        Long currentUserId = currentUser.getId();

        Long ownerUserId = request.getAnimal() != null && request.getAnimal().getOwnerUser() != null
                ? request.getAnimal().getOwnerUser().getId()
                : null;

        if (ownerUserId == null || !ownerUserId.equals(currentUserId)) {
            log.warn("Forbidden request action. username={}, requestId={}", username, request.getId());
            throw new IllegalStateException("Forbidden: only animal owner can manage this request");
        }
    }

    private void mustBeRequester(String username, AdoptionRequest request) {
        User currentUser = mustFindUser(username);
        Long currentUserId = currentUser.getId();

        Long requesterUserId = request.getUser() != null ? request.getUser().getId() : null;

        if (requesterUserId == null || !requesterUserId.equals(currentUserId)) {
            log.warn("Forbidden request cancel. username={}, requestId={}", username, request.getId());
            throw new IllegalStateException("Forbidden: only request owner can cancel this request");
        }
    }

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

    @Override
    @Transactional(readOnly = true)
    public Optional<AdoptionRequestDto> findById(Long id) {
        return requestRepository.findById(id).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdoptionRequestDto> findMine(String username) {
        User user = mustFindUser(username);

        return requestRepository.findByUser_IdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdoptionRequestDto> findReceived(String username) {
        User user = mustFindUser(username);

        return requestRepository.findByAnimal_OwnerUser_IdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public AdoptionRequestDto create(String username, AdoptionRequestDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("request dto is required");
        }

        User user = mustFindUser(username);

        log.info("Creating adoption request for username={} animalId={}", username, dto.getAnimalId());

        Animal animal = animalRepository.findById(dto.getAnimalId())
                .orElseThrow(() -> {
                    log.warn("Animal not found for adoption request. animalId={}", dto.getAnimalId());
                    return new IllegalArgumentException("Animal not found");
                });

        if (animal.getOwnerUser() != null && animal.getOwnerUser().getId().equals(user.getId())) {
            log.warn("User cannot adopt own animal. userId={} animalId={}", user.getId(), animal.getId());
            throw new IllegalStateException("You cannot adopt your own animal listing");
        }

        if (!String.valueOf(AnimalStatus.AVAILABLE).equals(animal.getStatus())) {
            log.warn("Cannot request adoption for non-available animal. animalId={} status={}", animal.getId(), animal.getStatus());
            throw new IllegalStateException("This animal is not available for adoption");
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

    @Override
    public Optional<AdoptionRequestDto> approve(String username, Long id) {
        log.info("Approving adoption request with id={} by username={}", id, username);

        return requestRepository.findById(id).map(request -> {
            mustBeAnimalOwner(username, request);

            if (!STATUS_PENDING.equals(request.getStatus())) {
                log.warn("Only pending requests can be approved. id={} status={}", id, request.getStatus());
                throw new IllegalStateException("Only pending requests can be approved");
            }

            Animal animal = request.getAnimal();
            if (animal == null) {
                throw new IllegalStateException("Request has no animal");
            }

            request.setStatus(STATUS_APPROVED);
            request.setReason(null);

            animal.setStatus(String.valueOf(AnimalStatus.ADOPTED));
            animalRepository.save(animal);

            List<AdoptionRequest> otherPendingRequests =
                    requestRepository.findByAnimal_IdAndStatus(animal.getId(), STATUS_PENDING);

            for (AdoptionRequest otherRequest : otherPendingRequests) {
                if (!otherRequest.getId().equals(request.getId())) {
                    otherRequest.setStatus(STATUS_REJECTED);
                    otherRequest.setReason("Another adoption request was approved for this animal");
                }
            }

            requestRepository.saveAll(otherPendingRequests);

            AdoptionRequest savedRequest = requestRepository.save(request);
            log.info("Adoption request approved successfully with id={}", id);

            return toDto(savedRequest);
        }).or(() -> {
            log.warn("Adoption request not found for approval. id={}", id);
            return Optional.empty();
        });
    }

    @Override
    public Optional<AdoptionRequestDto> reject(String username, Long id, String reason) {
        log.info("Rejecting adoption request with id={} by username={}", id, username);

        return requestRepository.findById(id).map(request -> {
            mustBeAnimalOwner(username, request);

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

    @Override
    public boolean cancel(String username, Long id) {
        log.info("Canceling adoption request with id={} by username={}", id, username);

        AdoptionRequest request = requestRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Adoption request not found for cancellation. id={}", id);
                    return new IllegalArgumentException("Request not found");
                });

        mustBeRequester(username, request);

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
