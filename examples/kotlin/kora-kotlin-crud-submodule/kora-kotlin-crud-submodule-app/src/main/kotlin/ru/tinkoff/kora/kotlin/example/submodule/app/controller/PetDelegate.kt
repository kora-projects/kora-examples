package ru.tinkoff.kora.kotlin.example.submodule.app.controller

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.kotlin.example.submodule.app.model.PetMapper
import ru.tinkoff.kora.kotlin.example.submodule.openapi.http.server.api.PetApiDelegate
import ru.tinkoff.kora.kotlin.example.submodule.openapi.http.server.api.PetApiResponses
import ru.tinkoff.kora.kotlin.example.submodule.openapi.http.server.model.MessageTO
import ru.tinkoff.kora.kotlin.example.submodule.openapi.http.server.model.PetCreateTO
import ru.tinkoff.kora.kotlin.example.submodule.openapi.http.server.model.PetUpdateTO
import ru.tinkoff.kora.kotlin.example.submodule.pet.model.dao.Pet
import ru.tinkoff.kora.kotlin.example.submodule.pet.service.PetService

@Component
class PetDelegate(
    private val petMapper: PetMapper,
    private val petService: PetService,
) : PetApiDelegate {
    override fun getPetById(id: Long): PetApiResponses.GetPetByIdApiResponse {
        if (id < 0) {
            return PetApiResponses.GetPetByIdApiResponse.GetPetById400ApiResponse(malformedId(id))
        }

        val pet = petService.findByID(id)
        return if (pet != null) {
            PetApiResponses.GetPetByIdApiResponse.GetPetById200ApiResponse(petMapper.asDTO(pet))
        } else {
            PetApiResponses.GetPetByIdApiResponse.GetPetById404ApiResponse(notFound(id))
        }
    }

    override fun addPet(petCreateTO: PetCreateTO): PetApiResponses.AddPetApiResponse {
        val pet = petService.add(petCreateTO.name, petCreateTO.category.name)
        return PetApiResponses.AddPetApiResponse.AddPet200ApiResponse(petMapper.asDTO(pet))
    }

    override fun updatePet(id: Long, petUpdateTO: PetUpdateTO): PetApiResponses.UpdatePetApiResponse {
        if (id < 0) {
            return PetApiResponses.UpdatePetApiResponse.UpdatePet400ApiResponse(malformedId(id))
        }

        val updated = petService.update(
            id,
            petUpdateTO.name,
            petUpdateTO.category?.name,
            toStatus(petUpdateTO.status),
        )
        return if (updated != null) {
            PetApiResponses.UpdatePetApiResponse.UpdatePet200ApiResponse(petMapper.asDTO(updated))
        } else {
            PetApiResponses.UpdatePetApiResponse.UpdatePet404ApiResponse(notFound(id))
        }
    }

    override fun deletePet(id: Long): PetApiResponses.DeletePetApiResponse {
        if (id < 0) {
            return PetApiResponses.DeletePetApiResponse.DeletePet400ApiResponse(malformedId(id))
        }

        return if (petService.delete(id)) {
            PetApiResponses.DeletePetApiResponse.DeletePet200ApiResponse(MessageTO("Successfully deleted Pet with ID: $id"))
        } else {
            PetApiResponses.DeletePetApiResponse.DeletePet404ApiResponse(notFound(id))
        }
    }

    private fun toStatus(statusEnum: PetUpdateTO.StatusEnum?): Pet.Status? {
        return when (statusEnum) {
            null -> null
            PetUpdateTO.StatusEnum.AVAILABLE -> Pet.Status.AVAILABLE
            PetUpdateTO.StatusEnum.PENDING -> Pet.Status.PENDING
            PetUpdateTO.StatusEnum.SOLD -> Pet.Status.SOLD
        }
    }

    private fun notFound(petId: Long): MessageTO = MessageTO("Pet not found for ID: $petId")
    private fun malformedId(petId: Long): MessageTO = MessageTO("Pet malformed ID: $petId")
}
