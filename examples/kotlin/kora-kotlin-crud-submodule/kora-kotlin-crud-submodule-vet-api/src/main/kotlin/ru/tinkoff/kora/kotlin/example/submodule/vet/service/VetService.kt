package ru.tinkoff.kora.kotlin.example.submodule.vet.service

import ru.tinkoff.kora.cache.annotation.CacheInvalidate
import ru.tinkoff.kora.cache.annotation.CachePut
import ru.tinkoff.kora.cache.annotation.Cacheable
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.kotlin.example.submodule.vet.model.dao.Vet
import ru.tinkoff.kora.kotlin.example.submodule.vet.repository.VetRepository
import ru.tinkoff.kora.resilient.circuitbreaker.annotation.CircuitBreaker
import ru.tinkoff.kora.resilient.retry.annotation.Retry
import ru.tinkoff.kora.resilient.timeout.annotation.Timeout

@Component
open class VetService(private val vetRepository: VetRepository) {
    @CircuitBreaker("vet")
    @Retry("vet")
    @Timeout("vet")
    open fun findAll(): List<Vet> = vetRepository.findAll()

    @Cacheable(VetCache::class)
    @CircuitBreaker("vet")
    @Retry("vet")
    @Timeout("vet")
    open fun findByID(vetId: Long): Vet? = vetRepository.findById(vetId)

    @CircuitBreaker("vet")
    @Timeout("vet")
    open fun add(name: String, surname: String): Vet {
        val vet = Vet(0, name, surname)
        return vet.copy(id = vetRepository.insert(vet))
    }

    @CircuitBreaker("vet")
    @Timeout("vet")
    @CachePut(value = VetCache::class, parameters = ["id"])
    open fun update(id: Long, name: String, surname: String): Vet? {
        val existing = vetRepository.findById(id) ?: return null
        val result = existing.copy(name = name, surname = surname)
        vetRepository.update(result)
        return result
    }

    @CircuitBreaker("vet")
    @Timeout("vet")
    @CacheInvalidate(VetCache::class)
    open fun delete(vetId: Long): Boolean = vetRepository.deleteById(vetId).value() == 1L
}
