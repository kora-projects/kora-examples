package ru.tinkoff.kora.kotlin.example.submodule.pet.service

import ru.tinkoff.kora.cache.annotation.CacheInvalidate
import ru.tinkoff.kora.cache.annotation.CachePut
import ru.tinkoff.kora.cache.annotation.Cacheable
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.kotlin.example.submodule.pet.model.dao.Pet
import ru.tinkoff.kora.kotlin.example.submodule.pet.model.dao.PetCategory
import ru.tinkoff.kora.kotlin.example.submodule.pet.model.dao.PetWithCategory
import ru.tinkoff.kora.kotlin.example.submodule.pet.repository.CategoryRepository
import ru.tinkoff.kora.kotlin.example.submodule.pet.repository.PetRepository
import ru.tinkoff.kora.resilient.circuitbreaker.annotation.CircuitBreaker
import ru.tinkoff.kora.resilient.retry.annotation.Retry
import ru.tinkoff.kora.resilient.timeout.annotation.Timeout

@Component
open class PetService(
    private val petRepository: PetRepository,
    private val categoryRepository: CategoryRepository,
) {
    @Cacheable(PetCache::class)
    @CircuitBreaker("pet")
    @Retry("pet")
    @Timeout("pet")
    open fun findByID(petId: Long): PetWithCategory? = petRepository.findById(petId)

    @CircuitBreaker("pet")
    @Timeout("pet")
    open fun add(petName: String, categoryName: String): PetWithCategory {
        val petCategoryId = categoryRepository.findByName(categoryName)?.id
            ?: categoryRepository.insert(categoryName)
        val pet = Pet(0, petName, Pet.Status.AVAILABLE, petCategoryId)
        val petId = petRepository.insert(pet)
        return PetWithCategory(petId, pet.name, pet.status, PetCategory(petCategoryId, categoryName))
    }

    @CircuitBreaker("pet")
    @Timeout("pet")
    @CachePut(value = PetCache::class, parameters = ["petId"])
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

    @CircuitBreaker("pet")
    @Timeout("pet")
    @CacheInvalidate(PetCache::class)
    open fun delete(petId: Long): Boolean = petRepository.deleteById(petId).value() == 1L
}
