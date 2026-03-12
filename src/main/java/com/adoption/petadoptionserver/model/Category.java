package com.adoption.petadoptionserver.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "categories",
        uniqueConstraints = @UniqueConstraint(name = "uk_category_name", columnNames = {"name"})
)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    public Category() {}

    public boolean isActive() { return active; }

    // getters / setters

    public Long getId() { return id; }
    public String getName() { return name; }
    public boolean getActive() { return active; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setActive(boolean active) {
        this.active = active;
    }
}