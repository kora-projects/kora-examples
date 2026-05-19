package ru.tinkoff.kora.kotlin.example.submodule.app.controller

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.kotlin.example.submodule.app.model.VetMapper
import ru.tinkoff.kora.kotlin.example.submodule.openapi.http.server.api.VetApiDelegate
import ru.tinkoff.kora.kotlin.example.submodule.openapi.http.server.api.VetApiResponses
import ru.tinkoff.kora.kotlin.example.submodule.openapi.http.server.model.MessageTO
import ru.tinkoff.kora.kotlin.example.submodule.openapi.http.server.model.VetCreateTO
import ru.tinkoff.kora.kotlin.example.submodule.openapi.http.server.model.VetUpdateTO
import ru.tinkoff.kora.kotlin.example.submodule.vet.service.VetService

@Component
class VetDelegate(
    private val vetMapper: VetMapper,
    private val vetService: VetService,
) : VetApiDelegate {
    override fun listVets(): VetApiResponses.ListVetsApiResponse {
        val vets = vetService.findAll().map(vetMapper::asDTO)
        return VetApiResponses.ListVetsApiResponse.ListVets200ApiResponse(vets)
    }

    override fun getVetById(vetId: Long): VetApiResponses.GetVetByIdApiResponse {
        if (vetId < 0) {
            return VetApiResponses.GetVetByIdApiResponse.GetVetById400ApiResponse(malformedId(vetId))
        }

        val vet = vetService.findByID(vetId)
        return if (vet != null) {
            VetApiResponses.GetVetByIdApiResponse.GetVetById200ApiResponse(vetMapper.asDTO(vet))
        } else {
            VetApiResponses.GetVetByIdApiResponse.GetVetById404ApiResponse(notFound(vetId))
        }
    }

    override fun addVet(vetCreateTO: VetCreateTO): VetApiResponses.AddVetApiResponse {
        val vet = vetService.add(vetCreateTO.firstName, vetCreateTO.lastName)
        return VetApiResponses.AddVetApiResponse.AddVet200ApiResponse(vetMapper.asDTO(vet))
    }

    override fun updateVet(vetId: Long, vetUpdateTO: VetUpdateTO): VetApiResponses.UpdateVetApiResponse {
        if (vetId < 0) {
            return VetApiResponses.UpdateVetApiResponse.UpdateVet400ApiResponse(malformedId(vetId))
        }

        val updated = vetService.update(vetId, vetUpdateTO.firstName, vetUpdateTO.lastName)
        return if (updated != null) {
            VetApiResponses.UpdateVetApiResponse.UpdateVet200ApiResponse(vetMapper.asDTO(updated))
        } else {
            VetApiResponses.UpdateVetApiResponse.UpdateVet404ApiResponse(notFound(vetId))
        }
    }

    override fun deleteVet(vetId: Long): VetApiResponses.DeleteVetApiResponse {
        if (vetId < 0) {
            return VetApiResponses.DeleteVetApiResponse.DeleteVet400ApiResponse(malformedId(vetId))
        }

        return if (vetService.delete(vetId)) {
            VetApiResponses.DeleteVetApiResponse.DeleteVet200ApiResponse(MessageTO("Successfully deleted Vet with ID: $vetId"))
        } else {
            VetApiResponses.DeleteVetApiResponse.DeleteVet404ApiResponse(notFound(vetId))
        }
    }

    private fun notFound(vetId: Long): MessageTO = MessageTO("Vet not found for ID: $vetId")
    private fun malformedId(vetId: Long): MessageTO = MessageTO("Vet malformed ID: $vetId")
}
