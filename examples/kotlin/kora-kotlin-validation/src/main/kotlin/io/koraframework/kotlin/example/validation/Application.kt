package io.koraframework.kotlin.example.validation

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.logging.logback.LogbackModule
import io.koraframework.validation.common.constraint.ValidatorModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, ValidatorModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
