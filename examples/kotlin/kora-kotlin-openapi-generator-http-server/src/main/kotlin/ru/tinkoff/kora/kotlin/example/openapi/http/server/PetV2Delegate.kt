package ru.tinkoff.kora.kotlin.example.openapi.http.server

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.kotlin.example.openapi.petV2.api.PetApiController
import ru.tinkoff.kora.kotlin.example.openapi.petV2.api.PetApiDelegate
import ru.tinkoff.kora.kotlin.example.openapi.petV2.api.PetApiResponses
import ru.tinkoff.kora.kotlin.example.openapi.petV2.model.Message
import ru.tinkoff.kora.kotlin.example.openapi.petV2.model.Pet
import java.util.concurrent.ConcurrentHashMap

@Component
class PetV2Delegate : PetApiDelegate {
    private val petMap = ConcurrentHashMap<Long, Pet>()

    override fun addPet(body: Pet): PetApiResponses.AddPetApiResponse {
        petMap[body.id] = body
        return PetApiResponses.AddPetApiResponse.AddPet200ApiResponse(Message("OK"))
    }

    override fun deletePet(petId: Long, apiKey: String?): PetApiResponses.DeletePetApiResponse {
        petMap.remove(petId)
        return PetApiResponses.DeletePetApiResponse.DeletePet200ApiResponse(Message("OK"))
    }

    override fun findPetsByStatus(status: List<String>): PetApiResponses.FindPetsByStatusApiResponse {
        val petStatuses = status.map(Pet.StatusEnum::fromValue).toSet()
        val pets = petMap.values.filter { petStatuses.contains(it.status) }
        return PetApiResponses.FindPetsByStatusApiResponse.FindPetsByStatus200ApiResponse(pets)
    }

    @Deprecated("Generated OpenAPI contract marks this operation as deprecated")
    override fun findPetsByTags(tags: List<String>): PetApiResponses.FindPetsByTagsApiResponse {
        val petTags = tags.toSet()
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

    override fun updatePet(body: Pet): PetApiResponses.UpdatePetApiResponse {
        if (!petMap.containsKey(body.id)) {
            return PetApiResponses.UpdatePetApiResponse.UpdatePet404ApiResponse()
        }

        petMap[body.id] = body
        return PetApiResponses.UpdatePetApiResponse.UpdatePet200ApiResponse(Message("OK"))
    }

    override fun updatePetWithForm(
        petId: Long,
        form: PetApiController.UpdatePetWithFormFormParam,
    ): PetApiResponses.UpdatePetWithFormApiResponse {
        val pet = petMap[petId] ?: return PetApiResponses.UpdatePetWithFormApiResponse.UpdatePetWithForm404ApiResponse()
        val updated = pet.copy(name = form.name, status = form.status?.let(Pet.StatusEnum::fromValue))
        petMap[updated.id] = updated
        return PetApiResponses.UpdatePetWithFormApiResponse.UpdatePetWithForm200ApiResponse(Message("OK"))
    }
}
