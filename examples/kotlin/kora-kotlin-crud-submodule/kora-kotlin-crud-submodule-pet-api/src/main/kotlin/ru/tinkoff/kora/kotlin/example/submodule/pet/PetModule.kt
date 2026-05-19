package ru.tinkoff.kora.kotlin.example.submodule.pet

import ru.tinkoff.kora.cache.caffeine.CaffeineCacheModule
import ru.tinkoff.kora.common.KoraSubmodule
import ru.tinkoff.kora.database.jdbc.JdbcDatabaseModule
import ru.tinkoff.kora.kotlin.example.submodule.common.CommonModule
import ru.tinkoff.kora.resilient.ResilientModule

@KoraSubmodule
interface PetModule : CommonModule, JdbcDatabaseModule, CaffeineCacheModule, ResilientModule
