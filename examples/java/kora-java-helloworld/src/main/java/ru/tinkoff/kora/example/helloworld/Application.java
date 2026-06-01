package ru.tinkoff.kora.example.helloworld;

import ru.tinkoff.kora.application.graph.KoraApplication;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.config.hocon.HoconConfigModule;
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule;
import ru.tinkoff.kora.json.module.JsonModule;
import ru.tinkoff.kora.logging.logback.LogbackModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        JsonModule,
        LogbackModule,
        UndertowHttpServerModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
