package io.koraframework.kotlin.example.submodule.app.model

import io.koraframework.common.annotation.Component
import io.koraframework.kotlin.example.submodule.openapi.http.server.model.VetTO
import io.koraframework.kotlin.example.submodule.vet.model.dao.Vet

interface VetMapper {
    fun asDTO(vet: Vet): VetTO
}

@Component
class ManualVetMapper : VetMapper {
    override fun asDTO(vet: Vet): VetTO = VetTO(vet.name, vet.surname, vet.id)
}
