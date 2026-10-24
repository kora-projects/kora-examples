package io.koraframework.example.submodule.pet;

import io.koraframework.cache.caffeine.CaffeineCacheModule;
import io.koraframework.common.annotation.KoraSubmodule;
import io.koraframework.database.jdbc.JdbcDatabaseModule;
import io.koraframework.example.submodule.common.CommonModule;
import io.koraframework.resilient.ResilientModule;

@KoraSubmodule
public interface PetModule extends
        CommonModule,
        JdbcDatabaseModule,
        CaffeineCacheModule,
        ResilientModule {

}
