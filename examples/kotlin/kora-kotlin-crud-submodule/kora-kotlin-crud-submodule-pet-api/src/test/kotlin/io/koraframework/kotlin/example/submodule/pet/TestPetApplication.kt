package io.koraframework.kotlin.example.submodule.pet

import io.koraframework.common.annotation.KoraApp
import io.koraframework.common.annotation.Root
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.kotlin.example.submodule.pet.service.PetService

@KoraApp
interface TestPetApplication : HoconConfigModule, PetModule {

    @Root
    fun root(petService: PetService): String = "root"
}
