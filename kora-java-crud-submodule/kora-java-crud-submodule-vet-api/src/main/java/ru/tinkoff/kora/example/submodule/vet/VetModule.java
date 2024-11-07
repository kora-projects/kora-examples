package ru.tinkoff.kora.example.submodule.vet;

import ru.tinkoff.kora.cache.caffeine.CaffeineCacheModule;
import ru.tinkoff.kora.common.KoraSubmodule;
import ru.tinkoff.kora.database.jdbc.JdbcDatabaseModule;
import ru.tinkoff.kora.example.submodule.common.CommonModule;
import ru.tinkoff.kora.resilient.ResilientModule;

@KoraSubmodule
public interface VetModule extends
        CommonModule,
        JdbcDatabaseModule,
        CaffeineCacheModule,
        ResilientModule {

}
