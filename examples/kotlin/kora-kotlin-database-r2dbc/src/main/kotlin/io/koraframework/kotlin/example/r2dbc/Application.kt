package io.koraframework.kotlin.example.r2dbc

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.database.r2dbc.R2dbcDatabaseModule
import io.koraframework.logging.logback.LogbackModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, R2dbcDatabaseModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
