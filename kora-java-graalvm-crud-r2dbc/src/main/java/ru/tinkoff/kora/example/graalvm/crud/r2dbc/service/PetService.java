package ru.tinkoff.kora.example.graalvm.crud.r2dbc.service;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import ru.tinkoff.kora.cache.annotation.CacheInvalidate;
import ru.tinkoff.kora.cache.annotation.CachePut;
import ru.tinkoff.kora.cache.annotation.Cacheable;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.example.graalvm.crud.openapi.server.model.PetCreateTO;
import ru.tinkoff.kora.example.graalvm.crud.openapi.server.model.PetUpdateTO;
import ru.tinkoff.kora.example.graalvm.crud.r2dbc.model.dao.Pet;
import ru.tinkoff.kora.example.graalvm.crud.r2dbc.model.dao.PetCategory;
import ru.tinkoff.kora.example.graalvm.crud.r2dbc.model.dao.PetWithCategory;
import ru.tinkoff.kora.example.graalvm.crud.r2dbc.repository.CategoryRepository;
import ru.tinkoff.kora.example.graalvm.crud.r2dbc.repository.PetRepository;
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
        return petRepository.findById(petId).blockOptional();
    }

    @CircuitBreaker("pet")
    @Timeout("pet")
    public PetWithCategory add(PetCreateTO createTO) {
        final long petCategoryId = categoryRepository.findByName(createTO.category().name()).blockOptional()
                .map(PetCategory::id)
                .orElseGet(() -> categoryRepository.insert(createTO.category().name()).block());

        var pet = new Pet(ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE), createTO.name(), Pet.Status.AVAILABLE,
                petCategoryId);
        var petId = petRepository.insert(pet).block();

        return new PetWithCategory(petId, pet.name(), pet.status(),
                new PetCategory(petCategoryId, createTO.category().name()));
    }

    @CircuitBreaker("pet")
    @Timeout("pet")
    @CachePut(value = PetCache.class, parameters = "id")
    public Optional<PetWithCategory> update(long id, PetUpdateTO updateTO) {
        final Optional<PetWithCategory> existing = petRepository.findById(id).blockOptional();
        if (existing.isEmpty()) {
            return Optional.empty();
        }

        var category = existing.get().category();
        if (updateTO.category() != null) {
            category = categoryRepository.findByName(updateTO.category().name())
                    .blockOptional()
                    .orElseGet(() -> {
                        final long newCategoryId = categoryRepository.insert(updateTO.category().name()).block();
                        return new PetCategory(newCategoryId, updateTO.category().name());
                    });
        }

        var status = (updateTO.status() == null)
                ? existing.get().status()
                : toStatus(updateTO.status());
        var result = new PetWithCategory(existing.get().id(), updateTO.name(), status, category);

        petRepository.update(result.getPet()).block();
        return Optional.of(result);
    }

    @CircuitBreaker("pet")
    @Timeout("pet")
    @CacheInvalidate(PetCache.class)
    public boolean delete(long petId) {
        return petRepository.deleteById(petId).block().value() == 1;
    }

    private static Pet.Status toStatus(PetUpdateTO.StatusEnum statusEnum) {
        return switch (statusEnum) {
            case AVAILABLE -> Pet.Status.AVAILABLE;
            case PENDING -> Pet.Status.PENDING;
            case SOLD -> Pet.Status.SOLD;
        };
    }
}
