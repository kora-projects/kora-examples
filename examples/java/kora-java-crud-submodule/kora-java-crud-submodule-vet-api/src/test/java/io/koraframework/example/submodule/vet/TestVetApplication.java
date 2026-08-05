package io.koraframework.example.submodule.vet;

import io.koraframework.common.annotation.KoraApp;
import io.koraframework.common.annotation.Root;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.example.submodule.vet.service.VetService;

@KoraApp
public interface TestVetApplication extends
        HoconConfigModule,
        VetModule {

    @Root
    default String root(VetService petService) {
        return "root";
    }
}
