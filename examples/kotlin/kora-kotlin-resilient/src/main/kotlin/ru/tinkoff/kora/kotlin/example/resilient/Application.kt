package ru.tinkoff.kora.kotlin.example.resilient

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.logging.logback.LogbackModule
import ru.tinkoff.kora.resilient.ResilientModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, ResilientModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
