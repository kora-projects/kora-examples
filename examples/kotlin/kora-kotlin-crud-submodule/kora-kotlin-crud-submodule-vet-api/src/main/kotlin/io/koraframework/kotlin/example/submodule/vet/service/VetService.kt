package io.koraframework.kotlin.example.submodule.vet.service

import io.koraframework.cache.annotation.CacheInvalidate
import io.koraframework.cache.annotation.CachePut
import io.koraframework.cache.annotation.Cacheable
import io.koraframework.common.annotation.Component
import io.koraframework.kotlin.example.submodule.vet.model.dao.Vet
import io.koraframework.kotlin.example.submodule.vet.repository.VetRepository
import io.koraframework.resilient.circuitbreaker.annotation.CircuitBreaker
import io.koraframework.resilient.retry.annotation.Retry
import io.koraframework.resilient.timeout.annotation.Timeout

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
    @CachePut(value = VetCache::class, args = ["id"])
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
