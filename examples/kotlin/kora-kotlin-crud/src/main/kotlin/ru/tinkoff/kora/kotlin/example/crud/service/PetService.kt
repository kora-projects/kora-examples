package ru.tinkoff.kora.kotlin.example.crud.service

import ru.tinkoff.kora.cache.annotation.CacheInvalidate
import ru.tinkoff.kora.cache.annotation.CachePut
import ru.tinkoff.kora.cache.annotation.Cacheable
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.example.crud.openapi.http.server.model.PetCreateTO
import ru.tinkoff.kora.example.crud.openapi.http.server.model.PetUpdateTO
import ru.tinkoff.kora.kotlin.example.crud.model.Pet
import ru.tinkoff.kora.kotlin.example.crud.model.PetCategory
import ru.tinkoff.kora.kotlin.example.crud.model.PetWithCategory
import ru.tinkoff.kora.kotlin.example.crud.repository.CategoryRepository
import ru.tinkoff.kora.kotlin.example.crud.repository.PetRepository
import ru.tinkoff.kora.resilient.circuitbreaker.annotation.CircuitBreaker
import ru.tinkoff.kora.resilient.retry.annotation.Retry
import ru.tinkoff.kora.resilient.timeout.annotation.Timeout
import java.util.function.Supplier

@Component
open class PetService(
    private val petRepository: PetRepository,
    private val categoryRepository: CategoryRepository
) {

    @Cacheable(PetCache::class)
    @CircuitBreaker("pet")
    @Retry("pet")
    @Timeout("pet")
    open fun findByID(petId: Long): PetWithCategory? {
        return petRepository.findById(petId)
    }

    @CircuitBreaker("pet")
    @Timeout("pet")
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

    @CircuitBreaker("pet")
    @Timeout("pet")
    @CachePut(value = PetCache::class, parameters = ["id"])
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

    @CircuitBreaker("pet")
    @Timeout("pet")
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
