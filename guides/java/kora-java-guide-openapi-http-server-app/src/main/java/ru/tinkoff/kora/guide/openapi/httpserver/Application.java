package ru.tinkoff.kora.guide.openapi.httpserver;

import ru.tinkoff.kora.application.graph.KoraApplication;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.config.hocon.HoconConfigModule;
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule;
import ru.tinkoff.kora.json.module.JsonModule;
import ru.tinkoff.kora.logging.logback.LogbackModule;
import ru.tinkoff.kora.openapi.management.OpenApiManagementModule;
import ru.tinkoff.kora.validation.module.ValidationModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        UndertowHttpServerModule,
        JsonModule,
        LogbackModule,
        ValidationModule,
        OpenApiManagementModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}

