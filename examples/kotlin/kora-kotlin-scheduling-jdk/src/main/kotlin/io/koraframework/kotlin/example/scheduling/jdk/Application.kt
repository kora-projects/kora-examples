package io.koraframework.kotlin.example.scheduling.jdk

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.logging.logback.LogbackModule
import io.koraframework.scheduling.jdk.SchedulingJdkModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, SchedulingJdkModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
