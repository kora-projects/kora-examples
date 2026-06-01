package ru.tinkoff.kora.kotlin.example.config.hocon

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.config.yaml.YamlConfigModule
import ru.tinkoff.kora.logging.logback.LogbackModule

@KoraApp
interface Application : YamlConfigModule, LogbackModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
