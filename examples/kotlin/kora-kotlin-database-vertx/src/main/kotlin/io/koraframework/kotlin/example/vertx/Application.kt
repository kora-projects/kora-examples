package io.koraframework.kotlin.example.vertx

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.database.vertx.VertxDatabaseModule
import io.koraframework.logging.logback.LogbackModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, VertxDatabaseModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
