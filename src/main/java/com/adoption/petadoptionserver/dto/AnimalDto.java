package com.adoption.petadoptionserver.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AnimalDto {

    private Long id;

    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name up to 100 chars")
    private String name;

    @Size(max = 500, message = "image up to 500 chars")
    private String image;

    @Size(max = 20, message = "gender up to 20 chars")
    private String gender;

    @Size(max = 50, message = "size up to 50 chars")
    private String size;

    @Min(value = 0, message = "age must be >= 0")
    private Integer age;

    @Size(max = 100, message = "category up to 100 chars")
    private String category;

    @Size(max = 200, message = "location up to 200 chars")
    private String location;

    @Size(max = 2000, message = "description up to 2000 chars")
    private String description;

    @Size(max = 100, message = "ownerName up to 100 chars")
    private String ownerName;

    @Size(max = 30, message = "ownerPhone up to 30 chars")
    private String ownerPhone;

    @Size(max = 50, message = "status up to 50 chars")
    private String status;

    public AnimalDto() {}

    public AnimalDto(Long id, String name, String image, String gender, String size, Integer age,
                     String category, String location, String description,
                     String ownerName, String ownerPhone, String status) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.gender = gender;
        this.size = size;
        this.age = age;
        this.category = category;
        this.location = location;
        this.description = description;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getImage() { return image; }
    public String getGender() { return gender; }
    public String getSize() { return size; }
    public Integer getAge() { return age; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public String getOwnerName() { return ownerName; }
    public String getOwnerPhone() { return ownerPhone; }
    public String getStatus() { return status; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setImage(String image) { this.image = image; }
    public void setGender(String gender) { this.gender = gender; }
    public void setSize(String size) { this.size = size; }
    public void setAge(Integer age) { this.age = age; }
    public void setCategory(String category) { this.category = category; }
    public void setLocation(String location) { this.location = location; }
    public void setDescription(String description) { this.description = description; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public void setOwnerPhone(String ownerPhone) { this.ownerPhone = ownerPhone; }
    public void setStatus(String status) { this.status = status; }
}