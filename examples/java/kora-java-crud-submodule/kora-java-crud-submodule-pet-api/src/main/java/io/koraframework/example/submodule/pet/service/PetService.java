package io.koraframework.example.submodule.pet.service;

import org.jspecify.annotations.Nullable;
import java.util.Optional;
import io.koraframework.cache.annotation.CacheInvalidate;
import io.koraframework.cache.annotation.CachePut;
import io.koraframework.cache.annotation.Cacheable;
import io.koraframework.common.annotation.Component;
import io.koraframework.example.submodule.pet.model.dao.Pet;
import io.koraframework.example.submodule.pet.model.dao.PetCategory;
import io.koraframework.example.submodule.pet.model.dao.PetWithCategory;
import io.koraframework.example.submodule.pet.repository.CategoryRepository;
import io.koraframework.example.submodule.pet.repository.PetRepository;
import io.koraframework.resilient.circuitbreaker.annotation.CircuitBreakable;
import io.koraframework.resilient.retry.annotation.Retryable;
import io.koraframework.resilient.timeout.annotation.Timeout;

@Component
public class PetService {

    private final PetRepository petRepository;
    private final CategoryRepository categoryRepository;

    public PetService(PetRepository petRepository, CategoryRepository categoryRepository) {
        this.petRepository = petRepository;
        this.categoryRepository = categoryRepository;
    }

    @Cacheable(PetCache.class)
    @CircuitBreakable(PetCircuitBreaker.class)
    @Retryable(PetRetry.class)
    @Timeout(PetTimeouter.class)
    public Optional<PetWithCategory> findByID(long petId) {
        return petRepository.findById(petId);
    }

    @CircuitBreakable(PetCircuitBreaker.class)
    @Timeout(PetTimeouter.class)
    public PetWithCategory add(String petName, String categoryName) {
        final long petCategoryId = categoryRepository.findByName(categoryName)
                .map(PetCategory::id)
                .orElseGet(() -> categoryRepository.insert(categoryName));

        var pet = new Pet(0, petName, Pet.Status.AVAILABLE, petCategoryId);
        var petId = petRepository.insert(pet);

        return new PetWithCategory(petId, pet.name(), pet.status(),
                new PetCategory(petCategoryId, categoryName));
    }

    @CircuitBreakable(PetCircuitBreaker.class)
    @Timeout(PetTimeouter.class)
    @CachePut(value = PetCache.class, args = "petId")
    public Optional<PetWithCategory> update(long petId,
                                            @Nullable String petNameUpdate,
                                            @Nullable String petCategoryUpdate,
                                            Pet.@Nullable Status petStatusUpdate) {
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

    @CircuitBreakable(PetCircuitBreaker.class)
    @Timeout(PetTimeouter.class)
    @CacheInvalidate(PetCache.class)
    public boolean delete(long petId) {
        return petRepository.deleteById(petId).value() == 1;
    }
}
