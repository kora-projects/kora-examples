package io.koraframework.example.submodule.vet.service;

import java.util.List;
import java.util.Optional;
import io.koraframework.cache.annotation.CacheInvalidate;
import io.koraframework.cache.annotation.CachePut;
import io.koraframework.cache.annotation.Cacheable;
import io.koraframework.common.annotation.Component;
import io.koraframework.example.submodule.vet.model.dao.Vet;
import io.koraframework.example.submodule.vet.repository.VetRepository;
import io.koraframework.resilient.circuitbreaker.annotation.CircuitBreakable;
import io.koraframework.resilient.retry.annotation.Retryable;
import io.koraframework.resilient.timeout.annotation.Timeout;

@Component
public class VetService {

    private final VetRepository vetRepository;

    public VetService(VetRepository vetRepository) {
        this.vetRepository = vetRepository;
    }

    @CircuitBreakable(VetCircuitBreaker.class)
    @Retryable(VetRetry.class)
    @Timeout(VetTimeouter.class)
    public List<Vet> findAll() {
        return vetRepository.findAll();
    }

    @Cacheable(VetCache.class)
    @CircuitBreakable(VetCircuitBreaker.class)
    @Retryable(VetRetry.class)
    @Timeout(VetTimeouter.class)
    public Optional<Vet> findByID(long vetId) {
        return vetRepository.findById(vetId);
    }

    @CircuitBreakable(VetCircuitBreaker.class)
    @Timeout(VetTimeouter.class)
    public Vet add(String name, String surname) {
        var vet = new Vet(0, name, surname);
        var vetId = vetRepository.insert(vet);
        return new Vet(vetId, vet.name(), vet.surname());
    }

    @CircuitBreakable(VetCircuitBreaker.class)
    @Timeout(VetTimeouter.class)
    @CachePut(value = VetCache.class, args = "id")
    public Optional<Vet> update(long id, String name, String surname) {
        final Optional<Vet> existing = vetRepository.findById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }

        var result = new Vet(existing.get().id(), name, surname);
        vetRepository.update(result);
        return Optional.of(result);
    }

    @CircuitBreakable(VetCircuitBreaker.class)
    @Timeout(VetTimeouter.class)
    @CacheInvalidate(VetCache.class)
    public boolean delete(long vetId) {
        return vetRepository.deleteById(vetId).value() == 1;
    }
}
