package io.koraframework.kotlin.example.submodule.vet.service

import io.koraframework.cache.annotation.CacheInvalidate
import io.koraframework.cache.annotation.CachePut
import io.koraframework.cache.annotation.Cacheable
import io.koraframework.common.annotation.Component
import io.koraframework.kotlin.example.submodule.vet.model.dao.Vet
import io.koraframework.kotlin.example.submodule.vet.repository.VetRepository
import io.koraframework.resilient.circuitbreaker.annotation.CircuitBreakable
import io.koraframework.resilient.retry.annotation.Retryable
import io.koraframework.resilient.timeout.annotation.Timeout

@Component
open class VetService(private val vetRepository: VetRepository) {
    @CircuitBreakable(VetCircuitBreaker::class)
    @Retryable(VetRetry::class)
    @Timeout(VetTimeouter::class)
    open fun findAll(): List<Vet> = vetRepository.findAll()

    @Cacheable(VetCache::class)
    @CircuitBreakable(VetCircuitBreaker::class)
    @Retryable(VetRetry::class)
    @Timeout(VetTimeouter::class)
    open fun findByID(vetId: Long): Vet? = vetRepository.findById(vetId)

    @CircuitBreakable(VetCircuitBreaker::class)
    @Timeout(VetTimeouter::class)
    open fun add(name: String, surname: String): Vet {
        val vet = Vet(0, name, surname)
        return vet.copy(id = vetRepository.insert(vet))
    }

    @CircuitBreakable(VetCircuitBreaker::class)
    @Timeout(VetTimeouter::class)
    @CachePut(value = VetCache::class, args = ["id"])
    open fun update(id: Long, name: String, surname: String): Vet? {
        val existing = vetRepository.findById(id) ?: return null
        val result = existing.copy(name = name, surname = surname)
        vetRepository.update(result)
        return result
    }

    @CircuitBreakable(VetCircuitBreaker::class)
    @Timeout(VetTimeouter::class)
    @CacheInvalidate(VetCache::class)
    open fun delete(vetId: Long): Boolean = vetRepository.deleteById(vetId).value() == 1L
}
