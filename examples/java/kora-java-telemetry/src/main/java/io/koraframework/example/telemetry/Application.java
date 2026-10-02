package io.koraframework.example.telemetry;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.database.jdbc.JdbcDatabaseModule;
import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule;
import io.koraframework.logging.logback.LogbackModule;
import io.koraframework.micrometer.module.MetricsModule;
import io.koraframework.opentelemetry.tracing.exporter.grpc.OpentelemetryGrpcExporterModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        MetricsModule,
        JdbcDatabaseModule,
        UndertowPublicHttpServerModule,
        OpentelemetryGrpcExporterModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
