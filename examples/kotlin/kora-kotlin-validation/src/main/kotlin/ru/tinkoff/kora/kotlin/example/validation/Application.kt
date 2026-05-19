package ru.tinkoff.kora.kotlin.example.validation

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.logging.logback.LogbackModule
import ru.tinkoff.kora.validation.common.constraint.ValidatorModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, ValidatorModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
