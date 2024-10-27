package ru.tinkoff.kora.example.submodule.app;

import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.common.annotation.Root;
import ru.tinkoff.kora.example.submodule.pet.service.PetService;
import ru.tinkoff.kora.example.submodule.vet.service.VetService;

@KoraApp
public interface TestApplication extends Application {

    @Root
    default String root(PetService petService, VetService vetService) {
        return "root";
    }
}
