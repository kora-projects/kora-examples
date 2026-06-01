package ru.tinkoff.kora.guide.cache

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.cache.caffeine.CaffeineCacheModule
import ru.tinkoff.kora.cache.redis.RedisCacheModule
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule
import ru.tinkoff.kora.json.module.JsonModule
import ru.tinkoff.kora.logging.logback.LogbackModule

@KoraApp
interface Application :
    HoconConfigModule,
    JsonModule,
    LogbackModule,
    UndertowHttpServerModule,
    CaffeineCacheModule,
    RedisCacheModule

fun main() {
    KoraApplication.run(ApplicationGraph::graph)
}
