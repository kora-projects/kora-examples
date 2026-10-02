package io.koraframework.kotlin.example.config.hocon

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.yaml.YamlConfigModule
import io.koraframework.logging.logback.LogbackModule

@KoraApp
interface Application : YamlConfigModule, LogbackModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
