package ru.tinkoff.kora.kotlin.example.cache.redis

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.cache.redis.RedisCacheModule
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.logging.logback.LogbackModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, RedisCacheModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
