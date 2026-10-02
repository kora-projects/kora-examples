package io.koraframework.kotlin.example.submodule.pet.service

import io.koraframework.cache.annotation.CacheInvalidate
import io.koraframework.cache.annotation.CachePut
import io.koraframework.cache.annotation.Cacheable
import io.koraframework.common.annotation.Component
import io.koraframework.kotlin.example.submodule.pet.model.dao.Pet
import io.koraframework.kotlin.example.submodule.pet.model.dao.PetCategory
import io.koraframework.kotlin.example.submodule.pet.model.dao.PetWithCategory
import io.koraframework.kotlin.example.submodule.pet.repository.CategoryRepository
import io.koraframework.kotlin.example.submodule.pet.repository.PetRepository
import io.koraframework.resilient.circuitbreaker.annotation.CircuitBreakable
import io.koraframework.resilient.retry.annotation.Retryable
import io.koraframework.resilient.timeout.annotation.Timeout

@Component
open class PetService(
    private val petRepository: PetRepository,
    private val categoryRepository: CategoryRepository,
) {
    @Cacheable(PetCache::class)
    @CircuitBreakable(PetCircuitBreaker::class)
    @Retryable(PetRetry::class)
    @Timeout(PetTimeouter::class)
    open fun findByID(petId: Long): PetWithCategory? = petRepository.findById(petId)

    @CircuitBreakable(PetCircuitBreaker::class)
    @Timeout(PetTimeouter::class)
    open fun add(petName: String, categoryName: String): PetWithCategory {
        val petCategoryId = categoryRepository.findByName(categoryName)?.id
            ?: categoryRepository.insert(categoryName)
        val pet = Pet(0, petName, Pet.Status.AVAILABLE, petCategoryId)
        val petId = petRepository.insert(pet)
        return PetWithCategory(petId, pet.name, pet.status, PetCategory(petCategoryId, categoryName))
    }

    @CircuitBreakable(PetCircuitBreaker::class)
    @Timeout(PetTimeouter::class)
    @CachePut(value = PetCache::class, args = ["petId"])
    open fun update(
        petId: Long,
        petNameUpdate: String?,
        petCategoryUpdate: String?,
        petStatusUpdate: Pet.Status?,
    ): PetWithCategory? {
        val existing = petRepository.findById(petId) ?: return null
        val category = petCategoryUpdate?.let {
            categoryRepository.findByName(it) ?: PetCategory(categoryRepository.insert(it), it)
        } ?: existing.category
        val result = PetWithCategory(
            existing.id,
            petNameUpdate ?: existing.name,
            petStatusUpdate ?: existing.status,
            category,
        )
        petRepository.update(result.getPet())
        return result
    }

    @CircuitBreakable(PetCircuitBreaker::class)
    @Timeout(PetTimeouter::class)
    @CacheInvalidate(PetCache::class)
    open fun delete(petId: Long): Boolean = petRepository.deleteById(petId).value() == 1L
}
