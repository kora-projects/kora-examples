package ru.tinkoff.kora.example.submodule.pet;

import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.common.annotation.Root;
import ru.tinkoff.kora.config.hocon.HoconConfigModule;
import ru.tinkoff.kora.example.submodule.pet.service.PetService;

@KoraApp
public interface PetApplication extends
        HoconConfigModule,
        PetModule {

    @Root
    default String root(PetService petService) {
        return "root";
    }
}
