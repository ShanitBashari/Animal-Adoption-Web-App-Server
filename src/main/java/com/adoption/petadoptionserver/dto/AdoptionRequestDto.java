package com.adoption.petadoptionserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdoptionRequestDto {
    private Long id;
    private Long animalId;
    private Long userId;
    private String userName;
    private String message;
    private String status;
    private String reason;
    private String createdAt;
    private String updatedAt;
}