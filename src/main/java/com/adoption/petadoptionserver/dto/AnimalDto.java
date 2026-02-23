package com.adoption.petadoptionserver.dto;

public class AnimalDto {

    private Long id;
    private String name;
    private String image;
    private String gender;
    private String size;
    private Integer age;
    private String category;
    private String location;
    private String description;
    private String ownerName;
    private String ownerPhone;
    private String status;

    public AnimalDto() {
    }

    public AnimalDto(
            Long id,
            String name,
            String image,
            String gender,
            String size,
            Integer age,
            String category,
            String location,
            String description,
            String ownerName,
            String ownerPhone,
            String status
    ) {
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

    // ===== Getters =====
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

    // ===== Setters =====
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