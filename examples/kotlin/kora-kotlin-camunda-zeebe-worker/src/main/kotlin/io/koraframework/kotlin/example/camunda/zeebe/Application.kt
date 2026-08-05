package io.koraframework.kotlin.example.camunda.zeebe

import io.koraframework.application.graph.KoraApplication
import io.koraframework.camunda.zeebe.worker.ZeebeWorkerModule
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.json.common.JsonModule
import io.koraframework.logging.logback.LogbackModule
import io.koraframework.scheduling.jdk.SchedulingJdkModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, JsonModule, SchedulingJdkModule, ZeebeWorkerModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
