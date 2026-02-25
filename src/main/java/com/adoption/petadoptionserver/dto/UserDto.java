package com.adoption.petadoptionserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;

    @NotBlank(message = "username is required")
    @Size(min = 3, max = 50, message = "username length must be between 3 and 50")
    private String username;

    @NotBlank(message = "email is required")
    @Email(message = "invalid email")
    @Size(max = 150)
    private String email;

    @Size(max = 200)
    private String fullName;

    @Size(max = 30)
    private String phone;

    private List<String> roles;

    private Boolean enabled;

    private String createdAt;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(min = 6, message = "password must be at least 6 characters")
    private String password;
}