package ru.tinkoff.kora.guide.observability;

import ru.tinkoff.kora.application.graph.KoraApplication;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.config.hocon.HoconConfigModule;
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule;
import ru.tinkoff.kora.json.module.JsonModule;
import ru.tinkoff.kora.logging.logback.LogbackModule;
import ru.tinkoff.kora.micrometer.module.MetricsModule;
import ru.tinkoff.kora.opentelemetry.tracing.exporter.http.OpentelemetryHttpExporterModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        JsonModule,
        LogbackModule,
        MetricsModule,
        UndertowHttpServerModule,
        OpentelemetryHttpExporterModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}

