package io.koraframework.kotlin.example.submodule.pet

import io.koraframework.cache.caffeine.CaffeineCacheModule
import io.koraframework.common.annotation.KoraSubmodule
import io.koraframework.database.jdbc.JdbcDatabaseModule
import io.koraframework.kotlin.example.submodule.common.CommonModule
import io.koraframework.resilient.ResilientModule

@KoraSubmodule
interface PetModule : CommonModule, JdbcDatabaseModule, CaffeineCacheModule, ResilientModule
