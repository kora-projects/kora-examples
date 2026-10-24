package io.koraframework.example.graalvm.crud.cassandra.service;

import java.util.concurrent.ThreadLocalRandom;
import org.jspecify.annotations.Nullable;
import io.koraframework.cache.annotation.CacheInvalidate;
import io.koraframework.cache.annotation.CachePut;
import io.koraframework.cache.annotation.Cacheable;
import io.koraframework.common.annotation.Component;
import io.koraframework.example.graalvm.crud.cassandra.model.dao.Pet;
import io.koraframework.example.graalvm.crud.cassandra.repository.PetRepository;
import io.koraframework.example.graalvm.crud.openapi.server.model.PetCreateTO;
import io.koraframework.example.graalvm.crud.openapi.server.model.PetUpdateTO;
import io.koraframework.resilient.circuitbreaker.annotation.CircuitBreakable;
import io.koraframework.resilient.retry.annotation.Retryable;
import io.koraframework.resilient.timeout.annotation.Timeout;

@Component
public class PetService {

    private final PetRepository petRepository;

    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Cacheable(PetCache.class)
    @CircuitBreakable(PetCircuitBreaker.class)
    @Retryable(PetRetry.class)
    @Timeout(PetTimeouter.class)
    @Nullable
    public Pet findByID(long petId) {
        return petRepository.findById(petId);
    }

    @CircuitBreakable(PetCircuitBreaker.class)
    @Timeout(PetTimeouter.class)
    public Pet add(PetCreateTO createTO) {
        final long petId = ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE);
        final Pet pet = new Pet(petId, createTO.name(), Pet.Status.AVAILABLE, createTO.category().name());
        petRepository.insert(pet);
        return pet;
    }

    @CircuitBreakable(PetCircuitBreaker.class)
    @Timeout(PetTimeouter.class)
    @CachePut(value = PetCache.class, args = "id")
    @Nullable
    public Pet update(long id, PetUpdateTO updateTO) {
        final Pet pet = petRepository.findById(id);
        if (pet == null) {
            return null;
        }

        var status = (updateTO.status() == null)
                ? pet.status()
                : toStatus(updateTO.status());

        var category = (updateTO.category() == null)
                ? pet.category()
                : updateTO.category().name();

        var petUpdate = new Pet(pet.id(), updateTO.name(), status, category);
        petRepository.update(petUpdate);
        return petUpdate;
    }

    @CircuitBreakable(PetCircuitBreaker.class)
    @Timeout(PetTimeouter.class)
    @CacheInvalidate(PetCache.class)
    public boolean delete(long petId) {
        // Cassandra DELETE reports no affected row count, so a completed statement is the only success signal
        petRepository.deleteById(petId);
        return true;
    }

    private static Pet.Status toStatus(PetUpdateTO.StatusEnum statusEnum) {
        return switch (statusEnum) {
            case AVAILABLE -> Pet.Status.AVAILABLE;
            case PENDING -> Pet.Status.PENDING;
            case SOLD -> Pet.Status.SOLD;
        };
    }
}
