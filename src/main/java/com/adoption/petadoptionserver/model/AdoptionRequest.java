package com.adoption.petadoptionserver.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "adoption_requests")
public class AdoptionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 2000)
    private String message;

    @Column(length = 2000)
    private String reason;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // getters / setters
    public Long getId() { return id; }
    public Animal getAnimal() { return animal; }
    public User getUser() { return user; }
    public String getMessage() { return message; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setAnimal(Animal animal) { this.animal = animal; }
    public void setUser(User user) { this.user = user; }
    public void setMessage(String message) { this.message = message; }
    public void setReason(String reason) { this.reason = reason; }
    public void setStatus(String status) { this.status = status; }
}