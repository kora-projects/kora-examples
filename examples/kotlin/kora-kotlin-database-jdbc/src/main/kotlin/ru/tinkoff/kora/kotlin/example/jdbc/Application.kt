package ru.tinkoff.kora.kotlin.example.jdbc

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.database.jdbc.JdbcDatabase
import ru.tinkoff.kora.database.jdbc.JdbcDatabaseModule
import ru.tinkoff.kora.json.common.JsonCommonModule
import ru.tinkoff.kora.logging.logback.LogbackModule
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@KoraApp
interface Application : HoconConfigModule, LogbackModule, JsonCommonModule, JdbcDatabaseModule {
    @Tag(JdbcDatabase::class)
    fun jdbcExecutor(): Executor =
        Executors.newFixedThreadPool(maxOf(Runtime.getRuntime().availableProcessors(), 2) * 2)
}

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
