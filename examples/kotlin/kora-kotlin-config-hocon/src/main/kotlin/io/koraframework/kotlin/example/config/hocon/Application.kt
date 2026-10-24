package io.koraframework.kotlin.example.config.hocon

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.logging.logback.LogbackModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
