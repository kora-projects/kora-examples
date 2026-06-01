package ru.tinkoff.kora.kotlin.example.cache.caffeine

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.cache.caffeine.CaffeineCacheModule
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.logging.logback.LogbackModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, CaffeineCacheModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
