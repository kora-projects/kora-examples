package ru.tinkoff.kora.example.submodule.pet.service;

import jakarta.annotation.Nullable;
import java.util.Optional;
import ru.tinkoff.kora.cache.annotation.CacheInvalidate;
import ru.tinkoff.kora.cache.annotation.CachePut;
import ru.tinkoff.kora.cache.annotation.Cacheable;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.example.submodule.pet.model.dao.Pet;
import ru.tinkoff.kora.example.submodule.pet.model.dao.PetCategory;
import ru.tinkoff.kora.example.submodule.pet.model.dao.PetWithCategory;
import ru.tinkoff.kora.example.submodule.pet.repository.CategoryRepository;
import ru.tinkoff.kora.example.submodule.pet.repository.PetRepository;
import ru.tinkoff.kora.resilient.circuitbreaker.annotation.CircuitBreaker;
import ru.tinkoff.kora.resilient.retry.annotation.Retry;
import ru.tinkoff.kora.resilient.timeout.annotation.Timeout;

@Component
public class PetService {

    private final PetRepository petRepository;
    private final CategoryRepository categoryRepository;

    public PetService(PetRepository petRepository, CategoryRepository categoryRepository) {
        this.petRepository = petRepository;
        this.categoryRepository = categoryRepository;
    }

    @Cacheable(PetCache.class)
    @CircuitBreaker("pet")
    @Retry("pet")
    @Timeout("pet")
    public Optional<PetWithCategory> findByID(long petId) {
        return petRepository.findById(petId);
    }

    @CircuitBreaker("pet")
    @Timeout("pet")
    public PetWithCategory add(String petName, String categoryName) {
        final long petCategoryId = categoryRepository.findByName(categoryName)
                .map(PetCategory::id)
                .orElseGet(() -> categoryRepository.insert(categoryName));

        var pet = new Pet(0, petName, Pet.Status.AVAILABLE, petCategoryId);
        var petId = petRepository.insert(pet);

        return new PetWithCategory(petId, pet.name(), pet.status(),
                new PetCategory(petCategoryId, categoryName));
    }

    @CircuitBreaker("pet")
    @Timeout("pet")
    @CachePut(value = PetCache.class, parameters = "petId")
    public Optional<PetWithCategory> update(long petId,
                                            @Nullable String petNameUpdate,
                                            @Nullable String petCategoryUpdate,
                                            @Nullable Pet.Status petStatusUpdate) {
        final Optional<PetWithCategory> existing = petRepository.findById(petId);
        if (existing.isEmpty()) {
            return Optional.empty();
        }

        var category = existing.get().category();
        if (petCategoryUpdate != null) {
            category = categoryRepository.findByName(petCategoryUpdate).orElseGet(() -> {
                final long newCategoryId = categoryRepository.insert(petCategoryUpdate);
                return new PetCategory(newCategoryId, petCategoryUpdate);
            });
        }

        var status = (petStatusUpdate == null)
                ? existing.get().status()
                : petStatusUpdate;
        var petName = (petNameUpdate == null)
                ? existing.get().name()
                : petNameUpdate;
        var result = new PetWithCategory(existing.get().id(), petName, status, category);

        petRepository.update(result.getPet());
        return Optional.of(result);
    }

    @CircuitBreaker("pet")
    @Timeout("pet")
    @CacheInvalidate(PetCache.class)
    public boolean delete(long petId) {
        return petRepository.deleteById(petId).value() == 1;
    }
}
