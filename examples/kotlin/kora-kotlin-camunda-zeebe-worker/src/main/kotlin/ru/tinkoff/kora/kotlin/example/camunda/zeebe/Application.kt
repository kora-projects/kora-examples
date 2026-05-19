package ru.tinkoff.kora.kotlin.example.camunda.zeebe

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.camunda.zeebe.worker.ZeebeWorkerModule
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.json.module.JsonModule
import ru.tinkoff.kora.logging.logback.LogbackModule
import ru.tinkoff.kora.scheduling.jdk.SchedulingJdkModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, JsonModule, SchedulingJdkModule, ZeebeWorkerModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
