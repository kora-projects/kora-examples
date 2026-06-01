package ru.tinkoff.kora.kotlin.example.openapi.http.server

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.kotlin.example.openapi.petV3.api.PetApiDelegate
import ru.tinkoff.kora.kotlin.example.openapi.petV3.api.PetApiResponses
import ru.tinkoff.kora.kotlin.example.openapi.petV3.model.Message
import ru.tinkoff.kora.kotlin.example.openapi.petV3.model.Pet
import java.util.concurrent.ConcurrentHashMap

@Component
class PetV3Delegate : PetApiDelegate {
    private val petMap = ConcurrentHashMap<Long, Pet>()

    override fun addPet(pet: Pet): PetApiResponses.AddPetApiResponse {
        petMap[pet.id] = pet
        return PetApiResponses.AddPetApiResponse.AddPet200ApiResponse(pet)
    }

    override fun deletePet(petId: Long, apiKey: String?): PetApiResponses.DeletePetApiResponse {
        petMap.remove(petId)
        return PetApiResponses.DeletePetApiResponse.DeletePet200ApiResponse(Message("OK"))
    }

    override fun findPetsByStatus(status: String?): PetApiResponses.FindPetsByStatusApiResponse {
        val statusEnum = status?.let(Pet.StatusEnum::fromValue)
            ?: return PetApiResponses.FindPetsByStatusApiResponse.FindPetsByStatus400ApiResponse()
        val pets = petMap.values.filter { statusEnum == it.status }
        return PetApiResponses.FindPetsByStatusApiResponse.FindPetsByStatus200ApiResponse(pets)
    }

    override fun findPetsByTags(tags: List<String>?): PetApiResponses.FindPetsByTagsApiResponse {
        val petTags = tags?.toSet() ?: emptySet()
        val pets = petMap.values
            .filter { it.tags != null }
            .filter { pet -> pet.tags!!.all { tag -> petTags.contains(tag.name) } }

        return if (pets.isEmpty()) {
            PetApiResponses.FindPetsByTagsApiResponse.FindPetsByTags400ApiResponse()
        } else {
            PetApiResponses.FindPetsByTagsApiResponse.FindPetsByTags200ApiResponse(pets)
        }
    }

    override fun getPetById(petId: Long): PetApiResponses.GetPetByIdApiResponse {
        if (petId < 0) {
            return PetApiResponses.GetPetByIdApiResponse.GetPetById400ApiResponse()
        }

        val pet = petMap[petId]
        return if (pet == null) {
            PetApiResponses.GetPetByIdApiResponse.GetPetById404ApiResponse()
        } else {
            PetApiResponses.GetPetByIdApiResponse.GetPetById200ApiResponse(pet)
        }
    }

    override fun updatePet(pet: Pet): PetApiResponses.UpdatePetApiResponse {
        if (!petMap.containsKey(pet.id)) {
            return PetApiResponses.UpdatePetApiResponse.UpdatePet404ApiResponse()
        }

        petMap[pet.id] = pet
        return PetApiResponses.UpdatePetApiResponse.UpdatePet200ApiResponse(pet)
    }

    override fun updatePetWithForm(
        petId: Long,
        name: String?,
        status: String?,
    ): PetApiResponses.UpdatePetWithFormApiResponse {
        val pet = petMap[petId] ?: return PetApiResponses.UpdatePetWithFormApiResponse.UpdatePetWithForm404ApiResponse()
        val updated = pet.copy(
            name = name ?: pet.name,
            status = status?.let(Pet.StatusEnum::fromValue) ?: pet.status,
        )
        petMap[updated.id] = updated
        return PetApiResponses.UpdatePetWithFormApiResponse.UpdatePetWithForm200ApiResponse(Message("OK"))
    }
}
