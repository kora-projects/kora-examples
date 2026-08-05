package io.koraframework.example.graalvm.crud.vertx.service;

import reactor.core.publisher.Mono;
import io.koraframework.cache.annotation.CacheInvalidate;
import io.koraframework.cache.annotation.CachePut;
import io.koraframework.cache.annotation.Cacheable;
import io.koraframework.common.annotation.Component;
import io.koraframework.example.graalvm.crud.openapi.server.model.PetCreateTO;
import io.koraframework.example.graalvm.crud.openapi.server.model.PetUpdateTO;
import io.koraframework.example.graalvm.crud.vertx.model.dao.Pet;
import io.koraframework.example.graalvm.crud.vertx.model.dao.PetCategory;
import io.koraframework.example.graalvm.crud.vertx.model.dao.PetWithCategory;
import io.koraframework.example.graalvm.crud.vertx.repository.CategoryRepository;
import io.koraframework.example.graalvm.crud.vertx.repository.PetRepository;
import io.koraframework.resilient.circuitbreaker.annotation.CircuitBreaker;
import io.koraframework.resilient.retry.annotation.Retry;
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
    @CircuitBreaker("pet")
    @Retry("pet")
    @Timeout("pet")
    public Mono<PetWithCategory> findByID(long petId) {
        return petRepository.findById(petId);
    }

    @CircuitBreaker("pet")
    @Timeout("pet")
    public Mono<PetWithCategory> add(PetCreateTO createTO) {
        return categoryRepository.findByName(createTO.category().name())
                .map(PetCategory::id)
                .switchIfEmpty(categoryRepository.insert(createTO.category().name()))
                .flatMap(categoryId -> {
                    var pet = new Pet(0, createTO.name(), Pet.Status.AVAILABLE, categoryId);
                    return petRepository.insert(pet)
                            .map(petId -> new PetWithCategory(petId, pet.name(), pet.status(),
                                    new PetCategory(categoryId, createTO.category().name())));
                });
    }

    @CircuitBreaker("pet")
    @Timeout("pet")
    @CachePut(value = PetCache.class, args = "id")
    public Mono<PetWithCategory> update(long id, PetUpdateTO updateTO) {
        return petRepository.findById(id)
                .flatMap(existingPet -> {
                    var categoryMono = Mono.just(existingPet.category());
                    if (updateTO.category() != null) {
                        categoryMono = categoryRepository.findByName(updateTO.category().name())
                                .switchIfEmpty(Mono.defer(() -> categoryRepository.insert(updateTO.category().name())
                                        .map(newCategoryId -> new PetCategory(newCategoryId, updateTO.category().name()))));
                    }

                    var status = (updateTO.status() == null)
                            ? existingPet.status()
                            : toStatus(updateTO.status());

                    return categoryMono
                            .flatMap(category -> {
                                var result = new PetWithCategory(existingPet.id(), updateTO.name(), status, category);
                                return petRepository.update(result.getPet()).then(Mono.just(result));
                            });
                });
    }

    @CircuitBreaker("pet")
    @Timeout("pet")
    @CacheInvalidate(PetCache.class)
    public Mono<Boolean> delete(long petId) {
        return petRepository.deleteById(petId).map(counter -> counter.value() == 1);
    }

    private static Pet.Status toStatus(PetUpdateTO.StatusEnum statusEnum) {
        return switch (statusEnum) {
            case AVAILABLE -> Pet.Status.AVAILABLE;
            case PENDING -> Pet.Status.PENDING;
            case SOLD -> Pet.Status.SOLD;
        };
    }
}
