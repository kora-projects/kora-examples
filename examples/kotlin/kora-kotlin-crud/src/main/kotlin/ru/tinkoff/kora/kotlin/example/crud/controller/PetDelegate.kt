package ru.tinkoff.kora.kotlin.example.crud.controller

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.example.crud.openapi.http.server.api.PetApiDelegate
import ru.tinkoff.kora.example.crud.openapi.http.server.api.PetApiResponses
import ru.tinkoff.kora.example.crud.openapi.http.server.model.*
import ru.tinkoff.kora.kotlin.example.crud.model.Pet
import ru.tinkoff.kora.kotlin.example.crud.model.PetMapper
import ru.tinkoff.kora.kotlin.example.crud.model.PetWithCategory
import ru.tinkoff.kora.kotlin.example.crud.service.PetService

@Component
class PetDelegate(
    private val petService: PetService,
    private val petMapper: PetMapper
) : PetApiDelegate {

    override fun getPetById(id: Long): PetApiResponses.GetPetByIdApiResponse {
        if (id < 0) {
            return PetApiResponses.GetPetByIdApiResponse.GetPetById400ApiResponse(malformedId(id))
        }

        val pet = petService.findByID(id)
        if (pet != null) {
            val body = petMapper.petWithCategoryToPetTO(pet)
            return PetApiResponses.GetPetByIdApiResponse.GetPetById200ApiResponse(body)
        } else {
            return PetApiResponses.GetPetByIdApiResponse.GetPetById404ApiResponse(notFound(id))
        }
    }

    override fun addPet(petCreateTO: PetCreateTO): PetApiResponses.AddPetApiResponse {
        val pet: PetWithCategory = petService.add(petCreateTO)
        val body = petMapper.petWithCategoryToPetTO(pet)
        return PetApiResponses.AddPetApiResponse.AddPet200ApiResponse(body)
    }

    override fun updatePet(id: Long, petUpdateTO: PetUpdateTO): PetApiResponses.UpdatePetApiResponse {
        if (id < 0) {
            return PetApiResponses.UpdatePetApiResponse.UpdatePet400ApiResponse(malformedId(id))
        }

        val updated = petService.update(id, petUpdateTO)
        if (updated != null) {
            val body = petMapper.petWithCategoryToPetTO(updated)
            return PetApiResponses.UpdatePetApiResponse.UpdatePet200ApiResponse(body)
        } else {
            return PetApiResponses.UpdatePetApiResponse.UpdatePet404ApiResponse(notFound(id))
        }
    }

    override fun deletePet(id: Long): PetApiResponses.DeletePetApiResponse {
        if (id < 0) {
            return PetApiResponses.DeletePetApiResponse.DeletePet400ApiResponse(malformedId(id))
        }

        return if (petService.delete(id)) {
            PetApiResponses.DeletePetApiResponse.DeletePet200ApiResponse(
                MessageTO("Successfully deleted pet with ID: $id")
            )
        } else {
            PetApiResponses.DeletePetApiResponse.DeletePet404ApiResponse(notFound(id))
        }
    }

    private fun notFound(petId: Long): MessageTO {
        return MessageTO("Pet not found for ID: $petId")
    }

    private fun malformedId(petId: Long): MessageTO {
        return MessageTO("Pet malformed ID: $petId")
    }
}