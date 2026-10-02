package io.koraframework.kotlin.example.jdbc

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.database.jdbc.JdbcDatabaseModule
import io.koraframework.json.common.JsonModule
import io.koraframework.logging.logback.LogbackModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, JsonModule, JdbcDatabaseModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
