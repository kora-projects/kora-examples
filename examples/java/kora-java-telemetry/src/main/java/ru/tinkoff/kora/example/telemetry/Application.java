package ru.tinkoff.kora.example.telemetry;

import ru.tinkoff.kora.application.graph.KoraApplication;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.config.hocon.HoconConfigModule;
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule;
import ru.tinkoff.kora.logging.logback.LogbackModule;
import ru.tinkoff.kora.micrometer.module.MetricsModule;
import ru.tinkoff.kora.opentelemetry.tracing.exporter.grpc.OpentelemetryGrpcExporterModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        MetricsModule,
        UndertowHttpServerModule,
        OpentelemetryGrpcExporterModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
