package com.adoption.petadoptionserver.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdoptionRequestDto {
    private Long id;

    @NotNull(message = "animalId is required")
    private Long animalId;

    @NotNull(message = "userId is required")
    private Long userId;

    private String userName;

    @Size(max = 2000, message = "message max 2000 chars")
    private String message;

    private String reason;

    private String status; // PENDING / APPROVED / REJECTED / CANCELED

    private String createdAt;
    private String updatedAt;
}