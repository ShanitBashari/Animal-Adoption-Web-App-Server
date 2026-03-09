package com.adoption.petadoptionserver.repository;

import com.adoption.petadoptionserver.model.Animal;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

    // ✅ for Home search
    @Query("""
        select a from Animal a
        left join a.category c
        where (:q is null or :q = '' or
               lower(a.name) like lower(concat('%', :q, '%')) or
               lower(a.location) like lower(concat('%', :q, '%')))
          and (:category is null or :category = '' or
               lower(c.name) = lower(:category))
    """)

    List<Animal> search(@Param("q") String q, @Param("category") String category);

    List<Animal> findByOwnerUser_IdOrderByIdDesc(Long ownerUserId);
}