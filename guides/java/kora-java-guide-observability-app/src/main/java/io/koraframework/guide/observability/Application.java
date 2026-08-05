package io.koraframework.guide.observability;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule;
import io.koraframework.json.common.JsonModule;
import io.koraframework.logging.logback.LogbackModule;
import io.koraframework.micrometer.module.MetricsModule;
import io.koraframework.opentelemetry.tracing.exporter.http.OpentelemetryHttpExporterModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        JsonModule,
        LogbackModule,
        MetricsModule,
        UndertowPublicHttpServerModule,
        OpentelemetryHttpExporterModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}

