package io.koraframework.example.submodule.pet;

import io.koraframework.common.annotation.KoraApp;
import io.koraframework.common.annotation.Root;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.example.submodule.pet.service.PetService;

@KoraApp
public interface TestPetApplication extends
        HoconConfigModule,
        PetModule {

    @Root
    default String root(PetService petService) {
        return "root";
    }
}
