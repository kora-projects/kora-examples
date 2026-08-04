package io.koraframework.example.crud.service;

import io.koraframework.cache.annotation.CacheInvalidate;
import io.koraframework.cache.annotation.CachePut;
import io.koraframework.cache.annotation.Cacheable;
import io.koraframework.common.annotation.Component;
import io.koraframework.resilient.circuitbreaker.annotation.CircuitBreakable;
import io.koraframework.resilient.retry.annotation.Retryable;
import io.koraframework.resilient.timeout.annotation.Timeout;
import io.koraframework.example.crud.openapi.http.server.model.PetCreateTO;
import io.koraframework.example.crud.openapi.http.server.model.PetUpdateTO;
import io.koraframework.example.crud.model.dao.Pet;
import io.koraframework.example.crud.model.dao.PetCategory;
import io.koraframework.example.crud.model.dao.PetWithCategory;
import io.koraframework.example.crud.repository.CategoryRepository;
import io.koraframework.example.crud.repository.PetRepository;

import java.util.Optional;

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
    public PetWithCategory add(PetCreateTO createTO) {
        final long petCategoryId = categoryRepository.findByName(createTO.category().name())
                .map(PetCategory::id)
                .orElseGet(() -> categoryRepository.insert(createTO.category().name()));

        var pet = new Pet(0, createTO.name(), Pet.Status.AVAILABLE, petCategoryId);
        var petId = petRepository.insert(pet);

        return new PetWithCategory(petId, pet.name(), pet.status(),
                new PetCategory(petCategoryId, createTO.category().name()));
    }

    @CircuitBreakable(PetCircuitBreaker.class)
    @Timeout(PetTimeouter.class)
    @CachePut(value = PetCache.class, args = "id")
    public Optional<PetWithCategory> update(long id, PetUpdateTO updateTO) {
        final Optional<PetWithCategory> existing = petRepository.findById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }

        var category = existing.get().category();
        if (updateTO.category() != null) {
            category = categoryRepository.findByName(updateTO.category().name()).orElseGet(() -> {
                final long newCategoryId = categoryRepository.insert(updateTO.category().name());
                return new PetCategory(newCategoryId, updateTO.category().name());
            });
        }

        var status = (updateTO.status() == null)
                ? existing.get().status()
                : toStatus(updateTO.status());
        var result = new PetWithCategory(existing.get().id(), updateTO.name(), status, category);

        petRepository.update(result.getPet());
        return Optional.of(result);
    }

    @CircuitBreakable(PetCircuitBreaker.class)
    @Timeout(PetTimeouter.class)
    @CacheInvalidate(PetCache.class)
    public boolean delete(long petId) {
        return petRepository.deleteById(petId).value() == 1;
    }

    private static Pet.Status toStatus(PetUpdateTO.StatusEnum statusEnum) {
        return switch (statusEnum) {
            case AVAILABLE -> Pet.Status.AVAILABLE;
            case PENDING -> Pet.Status.PENDING;
            case SOLD -> Pet.Status.SOLD;
        };
    }
}
