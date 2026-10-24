package io.koraframework.guide.cache

import io.koraframework.application.graph.KoraApplication
import io.koraframework.cache.caffeine.CaffeineCacheModule
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule
import io.koraframework.json.common.JsonModule
import io.koraframework.logging.logback.LogbackModule

@KoraApp
interface Application :
    HoconConfigModule,
    JsonModule,
    LogbackModule,
    UndertowPublicHttpServerModule,
    CaffeineCacheModule

fun main() {
    KoraApplication.run(ApplicationGraph::graph)
}
