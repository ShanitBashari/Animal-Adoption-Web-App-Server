package com.adoption.petadoptionserver.repository;

import com.adoption.petadoptionserver.model.Animal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

    @Query("""
        SELECT a FROM Animal a
        WHERE (:category IS NULL OR :category = '' OR LOWER(a.category.name) = LOWER(:category))
          AND (:q IS NULL OR :q = '' OR
               LOWER(a.name) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(a.description) LIKE LOWER(CONCAT('%', :q, '%')))
    """)
    List<Animal> search(@Param("q") String q, @Param("category") String category);
}