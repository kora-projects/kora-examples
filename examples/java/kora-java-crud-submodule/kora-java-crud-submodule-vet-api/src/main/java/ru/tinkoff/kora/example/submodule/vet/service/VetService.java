package ru.tinkoff.kora.example.submodule.vet.service;

import java.util.List;
import java.util.Optional;
import ru.tinkoff.kora.cache.annotation.CacheInvalidate;
import ru.tinkoff.kora.cache.annotation.CachePut;
import ru.tinkoff.kora.cache.annotation.Cacheable;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.example.submodule.vet.model.dao.Vet;
import ru.tinkoff.kora.example.submodule.vet.repository.VetRepository;
import ru.tinkoff.kora.resilient.circuitbreaker.annotation.CircuitBreaker;
import ru.tinkoff.kora.resilient.retry.annotation.Retry;
import ru.tinkoff.kora.resilient.timeout.annotation.Timeout;

@Component
public class VetService {

    private final VetRepository vetRepository;

    public VetService(VetRepository vetRepository) {
        this.vetRepository = vetRepository;
    }

    @CircuitBreaker("vet")
    @Retry("vet")
    @Timeout("vet")
    public List<Vet> findAll() {
        return vetRepository.findAll();
    }

    @Cacheable(VetCache.class)
    @CircuitBreaker("vet")
    @Retry("vet")
    @Timeout("vet")
    public Optional<Vet> findByID(long vetId) {
        return vetRepository.findById(vetId);
    }

    @CircuitBreaker("vet")
    @Timeout("vet")
    public Vet add(String name, String surname) {
        var vet = new Vet(0, name, surname);
        var vetId = vetRepository.insert(vet);
        return new Vet(vetId, vet.name(), vet.surname());
    }

    @CircuitBreaker("vet")
    @Timeout("vet")
    @CachePut(value = VetCache.class, parameters = "id")
    public Optional<Vet> update(long id, String name, String surname) {
        final Optional<Vet> existing = vetRepository.findById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }

        var result = new Vet(existing.get().id(), name, surname);
        vetRepository.update(result);
        return Optional.of(result);
    }

    @CircuitBreaker("vet")
    @Timeout("vet")
    @CacheInvalidate(VetCache.class)
    public boolean delete(long vetId) {
        return vetRepository.deleteById(vetId).value() == 1;
    }
}
