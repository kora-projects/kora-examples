package ru.tinkoff.kora.kotlin.example.config.hocon

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.logging.logback.LogbackModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
