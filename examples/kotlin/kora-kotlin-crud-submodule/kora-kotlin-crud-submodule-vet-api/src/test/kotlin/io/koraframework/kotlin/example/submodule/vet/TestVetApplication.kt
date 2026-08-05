package io.koraframework.kotlin.example.submodule.vet

import io.koraframework.common.annotation.KoraApp
import io.koraframework.common.annotation.Root
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.kotlin.example.submodule.vet.service.VetService

@KoraApp
interface TestVetApplication : HoconConfigModule, VetModule {

    @Root
    fun root(vetService: VetService): String = "root"
}
