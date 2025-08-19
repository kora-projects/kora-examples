package ru.tinkoff.kora.kotlin.example.crud.model

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.example.crud.openapi.http.server.model.CategoryTO
import ru.tinkoff.kora.example.crud.openapi.http.server.model.PetTO
import ru.tinkoff.kora.example.crud.openapi.http.server.model.PetTO.StatusEnum

interface PetMapper {

    fun petWithCategoryToPetTO(pet: PetWithCategory): PetTO

    fun petCategoryToCategoryTO(category: PetCategory): CategoryTO
}

@Component
class ManualPetMapper : PetMapper {

    override fun petWithCategoryToPetTO(pet: PetWithCategory): PetTO {
        val enum: StatusEnum = when (pet.status) {
            Pet.Status.AVAILABLE -> StatusEnum.AVAILABLE
            Pet.Status.PENDING -> StatusEnum.PENDING
            Pet.Status.SOLD -> StatusEnum.SOLD
        }
        return PetTO(pet.id, pet.name, enum, petCategoryToCategoryTO(pet.category))
    }

    override fun petCategoryToCategoryTO(category: PetCategory): CategoryTO {
        return CategoryTO(category.id, category.name)
    }
}