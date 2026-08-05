package io.koraframework.kotlin.example.telemetry

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule
import io.koraframework.logging.logback.LogbackModule
import io.koraframework.micrometer.module.MetricsModule
import io.koraframework.opentelemetry.tracing.exporter.grpc.OpentelemetryGrpcExporterModule

@KoraApp
interface Application : HoconConfigModule,
    LogbackModule,
    MetricsModule,
    UndertowPublicHttpServerModule,
    OpentelemetryGrpcExporterModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
