package io.koraframework.kotlin.example.kafka

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.json.common.JsonModule
import io.koraframework.kafka.common.KafkaModule
import io.koraframework.logging.logback.LogbackModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, JsonModule, KafkaModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
