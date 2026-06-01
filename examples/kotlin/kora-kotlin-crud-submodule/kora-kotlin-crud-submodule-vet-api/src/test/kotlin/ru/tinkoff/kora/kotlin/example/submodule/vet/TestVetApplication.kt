package ru.tinkoff.kora.kotlin.example.submodule.vet

import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.kotlin.example.submodule.vet.service.VetService

@KoraApp
interface TestVetApplication : HoconConfigModule, VetModule {

    @Root
    fun root(vetService: VetService): String = "root"
}
