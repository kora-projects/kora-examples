package io.koraframework.kotlin.example.submodule.vet

import io.koraframework.cache.caffeine.CaffeineCacheModule
import io.koraframework.common.annotation.KoraSubmodule
import io.koraframework.database.jdbc.JdbcDatabaseModule
import io.koraframework.kotlin.example.submodule.common.CommonModule
import io.koraframework.resilient.ResilientModule

@KoraSubmodule
interface VetModule : CommonModule, JdbcDatabaseModule, CaffeineCacheModule, ResilientModule
