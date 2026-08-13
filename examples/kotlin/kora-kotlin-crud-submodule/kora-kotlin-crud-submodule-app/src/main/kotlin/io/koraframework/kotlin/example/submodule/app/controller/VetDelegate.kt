package io.koraframework.kotlin.example.submodule.app.controller

import io.koraframework.common.annotation.Component
import io.koraframework.kotlin.example.submodule.app.model.VetMapper
import io.koraframework.kotlin.example.submodule.openapi.http.server.api.VetApiDelegate
import io.koraframework.kotlin.example.submodule.openapi.http.server.api.VetApiResponses
import io.koraframework.kotlin.example.submodule.openapi.http.server.model.MessageTO
import io.koraframework.kotlin.example.submodule.openapi.http.server.model.VetCreateTO
import io.koraframework.kotlin.example.submodule.vet.service.VetService

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

    override fun updateVet(vetId: Long, vetUpdateTO: VetCreateTO): VetApiResponses.UpdateVetApiResponse {
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
