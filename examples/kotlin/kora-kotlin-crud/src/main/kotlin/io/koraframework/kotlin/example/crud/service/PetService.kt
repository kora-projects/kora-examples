package io.koraframework.kotlin.example.crud.service

import io.koraframework.cache.annotation.CacheInvalidate
import io.koraframework.cache.annotation.CachePut
import io.koraframework.cache.annotation.Cacheable
import io.koraframework.common.annotation.Component
import io.koraframework.example.crud.openapi.http.server.model.PetCreateTO
import io.koraframework.example.crud.openapi.http.server.model.PetUpdateTO
import io.koraframework.kotlin.example.crud.model.Pet
import io.koraframework.kotlin.example.crud.model.PetCategory
import io.koraframework.kotlin.example.crud.model.PetWithCategory
import io.koraframework.kotlin.example.crud.repository.CategoryRepository
import io.koraframework.kotlin.example.crud.repository.PetRepository
import io.koraframework.resilient.circuitbreaker.annotation.CircuitBreakable
import io.koraframework.resilient.retry.annotation.Retryable
import io.koraframework.resilient.timeout.annotation.Timeout

@Component
open class PetService(
    private val petRepository: PetRepository,
    private val categoryRepository: CategoryRepository
) {

    @Cacheable(PetCache::class)
    @CircuitBreakable(PetCircuitBreaker::class)
    @Retryable(PetRetry::class)
    @Timeout(PetTimeouter::class)
    open fun findByID(petId: Long): PetWithCategory? {
        return petRepository.findById(petId)
    }

    @CircuitBreakable(PetCircuitBreaker::class)
    @Timeout(PetTimeouter::class)
    open fun add(createTO: PetCreateTO): PetWithCategory {
        val petCategoryId = categoryRepository.findByName(createTO.category.name)?.id
            ?: categoryRepository.insert(createTO.category.name)

        val pet = Pet(0, createTO.name, Pet.Status.AVAILABLE, petCategoryId)
        val petId = petRepository.insert(pet)

        return PetWithCategory(
            petId.toLong(), pet.name, pet.status,
            PetCategory(petCategoryId, createTO.category.name)
        )
    }

    @CircuitBreakable(PetCircuitBreaker::class)
    @Timeout(PetTimeouter::class)
    @CachePut(value = PetCache::class, args = ["id"])
    open fun update(id: Long, updateTO: PetUpdateTO): PetWithCategory? {
        val existing = petRepository.findById(id) ?: return null

        var category = existing.category
        if (updateTO.category != null) {
            category = categoryRepository.findByName(updateTO.category.name)
                ?: PetCategory(categoryRepository.insert(updateTO.category.name), updateTO.category.name)
        }

        val status = if (updateTO.status == null) existing.status else toStatus(updateTO.status)
        val result = PetWithCategory(existing.id, updateTO.name, status, category)
        petRepository.update(result.getPet())
        return result
    }

    @CircuitBreakable(PetCircuitBreaker::class)
    @Timeout(PetTimeouter::class)
    @CacheInvalidate(PetCache::class)
    open fun delete(petId: Long): Boolean {
        return petRepository.deleteById(petId).value() == 1L
    }

    companion object {
        private fun toStatus(statusEnum: PetUpdateTO.StatusEnum): Pet.Status {
            return when (statusEnum) {
                PetUpdateTO.StatusEnum.AVAILABLE -> Pet.Status.AVAILABLE
                PetUpdateTO.StatusEnum.PENDING -> Pet.Status.PENDING
                PetUpdateTO.StatusEnum.SOLD -> Pet.Status.SOLD
            }
        }
    }
}
