package ru.tinkoff.kora.kotlin.example.crud

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.cache.caffeine.CaffeineCacheModule
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.database.jdbc.JdbcDatabase
import ru.tinkoff.kora.database.jdbc.JdbcDatabaseModule
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule
import ru.tinkoff.kora.json.module.JsonModule
import ru.tinkoff.kora.logging.logback.LogbackModule
import ru.tinkoff.kora.micrometer.module.MetricsModule
import ru.tinkoff.kora.openapi.management.OpenApiManagementModule
import ru.tinkoff.kora.resilient.ResilientModule
import ru.tinkoff.kora.validation.module.ValidationModule
import java.util.concurrent.Executor


@KoraApp
interface Application : HoconConfigModule, LogbackModule, JdbcDatabaseModule, ValidationModule, JsonModule,
    CaffeineCacheModule, ResilientModule, MetricsModule, OpenApiManagementModule, UndertowHttpServerModule {

    @Tag(JdbcDatabase::class)
    fun dbExecutor(): Executor {
        val asExecutor = Dispatchers.IO.asExecutor()
        return Executor { command -> asExecutor.execute(command) }
    }
}

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
