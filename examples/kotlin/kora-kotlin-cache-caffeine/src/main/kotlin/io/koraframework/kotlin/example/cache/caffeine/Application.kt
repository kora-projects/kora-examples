package io.koraframework.kotlin.example.cache.caffeine

import io.koraframework.application.graph.KoraApplication
import io.koraframework.cache.caffeine.CaffeineCacheModule
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.logging.logback.LogbackModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, CaffeineCacheModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
