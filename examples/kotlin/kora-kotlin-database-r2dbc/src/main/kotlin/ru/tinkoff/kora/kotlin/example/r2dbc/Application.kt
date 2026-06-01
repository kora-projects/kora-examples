package ru.tinkoff.kora.kotlin.example.r2dbc

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.database.r2dbc.R2dbcDatabaseModule
import ru.tinkoff.kora.logging.logback.LogbackModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, R2dbcDatabaseModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
