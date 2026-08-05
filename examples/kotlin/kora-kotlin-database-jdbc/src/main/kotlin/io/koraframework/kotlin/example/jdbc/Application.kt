package io.koraframework.kotlin.example.jdbc

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.common.annotation.Tag
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.database.jdbc.JdbcDatabase
import io.koraframework.database.jdbc.JdbcDatabaseModule
import io.koraframework.json.common.JsonCommonModule
import io.koraframework.logging.logback.LogbackModule
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
