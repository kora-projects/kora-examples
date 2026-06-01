package ru.tinkoff.kora.kotlin.example.scheduling.jdk

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.logging.logback.LogbackModule
import ru.tinkoff.kora.scheduling.jdk.SchedulingJdkModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, SchedulingJdkModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
