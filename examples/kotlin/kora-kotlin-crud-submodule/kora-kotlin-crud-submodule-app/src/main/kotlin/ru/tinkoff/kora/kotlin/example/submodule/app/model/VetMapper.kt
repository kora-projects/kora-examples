package ru.tinkoff.kora.kotlin.example.submodule.app.model

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.kotlin.example.submodule.openapi.http.server.model.VetTO
import ru.tinkoff.kora.kotlin.example.submodule.vet.model.dao.Vet

interface VetMapper {
    fun asDTO(vet: Vet): VetTO
}

@Component
class ManualVetMapper : VetMapper {
    override fun asDTO(vet: Vet): VetTO = VetTO(vet.name, vet.surname, vet.id)
}
