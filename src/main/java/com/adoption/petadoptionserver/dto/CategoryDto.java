package com.adoption.petadoptionserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryDto {
    private Long id;

    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name max 100 chars")
    private String name;

    public CategoryDto() {}

    public CategoryDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}