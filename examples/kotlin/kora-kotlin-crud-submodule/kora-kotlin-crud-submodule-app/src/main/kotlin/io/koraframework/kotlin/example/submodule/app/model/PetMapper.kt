package io.koraframework.kotlin.example.submodule.app.model

import io.koraframework.common.annotation.Component
import io.koraframework.kotlin.example.submodule.openapi.http.server.model.CategoryTO
import io.koraframework.kotlin.example.submodule.openapi.http.server.model.PetTO
import io.koraframework.kotlin.example.submodule.pet.model.dao.Pet
import io.koraframework.kotlin.example.submodule.pet.model.dao.PetCategory
import io.koraframework.kotlin.example.submodule.pet.model.dao.PetWithCategory

interface PetMapper {
    fun asDTO(pet: PetWithCategory): PetTO
    fun asDTO(category: PetCategory): CategoryTO
}

@Component
class ManualPetMapper : PetMapper {
    override fun asDTO(pet: PetWithCategory): PetTO {
        val status = when (pet.status) {
            Pet.Status.AVAILABLE -> PetTO.StatusEnum.AVAILABLE
            Pet.Status.PENDING -> PetTO.StatusEnum.PENDING
            Pet.Status.SOLD -> PetTO.StatusEnum.SOLD
        }
        // the 2.0 generator orders the constructor by optionality, so the arguments are named
        return PetTO(status = status, id = pet.id, name = pet.name, category = asDTO(pet.category))
    }

    override fun asDTO(category: PetCategory): CategoryTO = CategoryTO(id = category.id, name = category.name)
}
