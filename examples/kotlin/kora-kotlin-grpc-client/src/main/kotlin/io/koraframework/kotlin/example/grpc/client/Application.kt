package io.koraframework.kotlin.example.grpc.client

import io.koraframework.grpc.client.GrpcClientModule
import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.logging.logback.LogbackModule

@KoraApp
interface Application : HoconConfigModule, LogbackModule, GrpcClientModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
