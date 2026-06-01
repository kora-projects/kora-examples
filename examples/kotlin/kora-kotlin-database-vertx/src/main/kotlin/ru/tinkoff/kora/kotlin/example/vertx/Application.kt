package ru.tinkoff.kora.kotlin.example.vertx

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.database.vertx.VertxDatabaseModule
import ru.tinkoff.kora.logging.logback.LogbackModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, VertxDatabaseModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
