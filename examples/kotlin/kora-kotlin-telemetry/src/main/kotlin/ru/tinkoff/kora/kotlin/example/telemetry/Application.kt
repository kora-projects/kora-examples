package ru.tinkoff.kora.kotlin.example.telemetry

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule
import ru.tinkoff.kora.logging.logback.LogbackModule
import ru.tinkoff.kora.micrometer.module.MetricsModule
import ru.tinkoff.kora.opentelemetry.tracing.exporter.grpc.OpentelemetryGrpcExporterModule

@KoraApp
interface Application : HoconConfigModule,
    LogbackModule,
    MetricsModule,
    UndertowHttpServerModule,
    OpentelemetryGrpcExporterModule

fun main() {
    KoraApplication.run { ApplicationGraph.graph() }
}
