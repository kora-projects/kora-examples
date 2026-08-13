package io.koraframework.kotlin.example.crud.model

import io.koraframework.common.annotation.Component
import io.koraframework.example.crud.openapi.http.server.model.CategoryTO
import io.koraframework.example.crud.openapi.http.server.model.PetTO
import io.koraframework.example.crud.openapi.http.server.model.PetTO.StatusEnum

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
        // the 2.0 generator orders the constructor by optionality, so the arguments are named
        return PetTO(status = enum, id = pet.id, name = pet.name, category = petCategoryToCategoryTO(pet.category))
    }

    override fun petCategoryToCategoryTO(category: PetCategory): CategoryTO {
        return CategoryTO(id = category.id, name = category.name)
    }
}
