package com.adoption.petadoptionserver.model;

import jakarta.persistence.*;

@Entity
@Table(name = "animals")
public class Animal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String image;
    private String gender;

    private String size;
    private Integer age;
    private String location;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(length = 2000)
    private String description;

    private String ownerName;
    private String ownerPhone;
    private String status;

    public Animal() {}

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getGender() {
        return gender;
    }

    public String getSize() {
        return size;
    }

    public Integer getAge() {
        return age;
    }

    public String getLocation() {
        return location;
    }

    public Category getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public String getStatus() {
        return status;
    }

}