package ru.tinkoff.kora.example.graalvm.crud.cassandra.service;

import java.util.concurrent.ThreadLocalRandom;
import reactor.core.publisher.Mono;
import ru.tinkoff.kora.cache.annotation.CacheInvalidate;
import ru.tinkoff.kora.cache.annotation.CachePut;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.example.graalvm.crud.cassandra.model.dao.Pet;
import ru.tinkoff.kora.example.graalvm.crud.cassandra.repository.PetRepository;
import ru.tinkoff.kora.example.graalvm.crud.openapi.server.model.PetCreateTO;
import ru.tinkoff.kora.example.graalvm.crud.openapi.server.model.PetUpdateTO;
import ru.tinkoff.kora.resilient.circuitbreaker.annotation.CircuitBreaker;
import ru.tinkoff.kora.resilient.retry.annotation.Retry;
import ru.tinkoff.kora.resilient.timeout.annotation.Timeout;

@Component
public class PetService {

    private final PetRepository petRepository;

    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    // TODO fixed in 1.1.1
    // @Cacheable(PetCache.class)
    @CircuitBreaker("pet")
    @Retry("pet")
    @Timeout("pet")
    public Mono<Pet> findByID(long petId) {
        return petRepository.findById(petId);
    }

    @CircuitBreaker("pet")
    @Timeout("pet")
    public Mono<Pet> add(PetCreateTO createTO) {
        final long petId = ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE);
        final Pet pet = new Pet(petId, createTO.name(), Pet.Status.AVAILABLE, createTO.category().name());
        return petRepository.insert(pet).then(Mono.just(pet));
    }

    @CircuitBreaker("pet")
    @Timeout("pet")
    @CachePut(value = PetCache.class, parameters = "id")
    public Mono<Pet> update(long id, PetUpdateTO updateTO) {
        return petRepository.findById(id)
                .flatMap(pet -> {
                    var status = (updateTO.status() == null)
                            ? pet.status()
                            : toStatus(updateTO.status());

                    var category = (updateTO.category() == null)
                            ? pet.category()
                            : updateTO.category().name();

                    var petUpdate = new Pet(pet.id(), updateTO.name(), status, category);
                    return petRepository.update(petUpdate).then(Mono.just(petUpdate));
                });
    }

    @CircuitBreaker("pet")
    @Timeout("pet")
    @CacheInvalidate(PetCache.class)
    public Mono<Boolean> delete(long petId) {
        return petRepository.deleteById(petId).thenReturn(true);
    }

    private static Pet.Status toStatus(PetUpdateTO.StatusEnum statusEnum) {
        return switch (statusEnum) {
            case AVAILABLE -> Pet.Status.AVAILABLE;
            case PENDING -> Pet.Status.PENDING;
            case SOLD -> Pet.Status.SOLD;
        };
    }
}
