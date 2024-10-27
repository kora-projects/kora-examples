package ru.tinkoff.kora.example.submodule.vet;

import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.common.annotation.Root;
import ru.tinkoff.kora.config.hocon.HoconConfigModule;
import ru.tinkoff.kora.example.submodule.vet.service.VetService;

@KoraApp
public interface VetApplication extends
        HoconConfigModule,
        VetModule {

    @Root
    default String root(VetService petService) {
        return "root";
    }
}
