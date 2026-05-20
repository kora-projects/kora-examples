package ru.tinkoff.kora.kotlin.example.submodule.pet

import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.kotlin.example.submodule.pet.service.PetService

@KoraApp
interface TestPetApplication : HoconConfigModule, PetModule {

    @Root
    fun root(petService: PetService): String = "root"
}
