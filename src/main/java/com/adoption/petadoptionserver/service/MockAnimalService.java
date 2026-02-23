package com.adoption.petadoptionserver.service;

import com.adoption.petadoptionserver.dto.AnimalDto;
import com.adoption.petadoptionserver.interfaces.AnimalService;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@Profile("mock")
public class MockAnimalService implements AnimalService {

    private final Map<Long, AnimalDto> store = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    @PostConstruct
    public void initSeed() {
        // seed sample animals
        create(new AnimalDto(null, "Max", "https://placedog.net/400/300", "Male",
                "Medium", 3, "Dog", "Tel Aviv", "Friendly and energetic", "David Cohen", "050-1234567", "Available"));
        create(new AnimalDto(null, "Bella", "https://placedog.net/500", "Female",
                "Small", 2, "Cat", "Haifa", "Shy but sweet", null, null, "Available"));
        create(new AnimalDto(null, "Bella", "https://placedog.net/502", "Female",
                "Small", 2, "Cat", "Haifa", "Shy but sweet", null, null, "Available"));
        create(new AnimalDto(null, "Jonny", "https://placedog.net/402/300", "Male",
                "Small", 2, "Cat", "Haifa", "Shy but sweet", null, null, "Available"));
        create(new AnimalDto(null, "Milky", "https://placedog.net/510", "Female",
                "Small", 2, "Cat", "Haifa", "Shy but sweet", null, null, "Available"));
        create(new AnimalDto(null, "Dobby", "https://placedog.net/505", "Female",
                "Small", 2, "Cat", "Haifa", "Shy but sweet", null, null, "Available"));
    }

    @Override
    public List<AnimalDto> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(a -> a.getId() == null ? 0L : a.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<AnimalDto> search(String q, String category) {
        String qNorm = q == null ? "" : q.trim().toLowerCase();
        String catNorm = category == null ? "" : category.trim().toLowerCase();

        return store.values().stream()
                .filter(a -> {
                    boolean matchesQ = qNorm.isEmpty() ||
                            (a.getName() != null && a.getName().toLowerCase().contains(qNorm)) ||
                            (a.getDescription() != null && a.getDescription().toLowerCase().contains(qNorm)) ||
                            (a.getLocation() != null && a.getLocation().toLowerCase().contains(qNorm));
                    boolean matchesCategory = catNorm.isEmpty() ||
                            (a.getCategory() != null && a.getCategory().toLowerCase().equals(catNorm));
                    return matchesQ && matchesCategory;
                })
                .sorted(Comparator.comparing(a -> a.getId() == null ? 0L : a.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AnimalDto> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public AnimalDto create(AnimalDto dto) {
        Long id = idGen.getAndIncrement();
        dto.setId(id);

        if (dto.getAge() == null) dto.setAge(0);

        store.put(id, dto);
        return dto;
    }

    @Override
    public Optional<AnimalDto> update(Long id, AnimalDto dto) {
        if (id == null) return Optional.empty();
        AnimalDto existing = store.get(id);
        if (existing == null) return Optional.empty();

        // החלפה מלאה של האובייקט (אפשר לשנות ל-merge לפי צורך)
        dto.setId(id);
        if (dto.getAge() == null) dto.setAge(existing.getAge());
        store.put(id, dto);
        return Optional.of(dto);
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) return false;
        return store.remove(id) != null;
    }
}