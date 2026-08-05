package io.koraframework.kotlin.example.resilient

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.logging.logback.LogbackModule
import io.koraframework.resilient.ResilientModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, ResilientModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
