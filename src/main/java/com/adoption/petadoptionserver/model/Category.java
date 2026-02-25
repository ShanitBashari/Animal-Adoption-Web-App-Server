package com.adoption.petadoptionserver.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categories",
        uniqueConstraints = @UniqueConstraint(name = "uk_category_name", columnNames = {"name"}))
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    public Category() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}