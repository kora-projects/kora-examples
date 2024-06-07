package ru.tinkoff.kora.example.camunda.engine;

import ru.tinkoff.kora.application.graph.KoraApplication;
import ru.tinkoff.kora.camunda.engine.CamundaEngineModule;
import ru.tinkoff.kora.camunda.rest.undertow.CamundaRestUndertowModule;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.config.hocon.HoconConfigModule;
import ru.tinkoff.kora.database.jdbc.JdbcDatabaseModule;
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule;
import ru.tinkoff.kora.json.module.JsonModule;
import ru.tinkoff.kora.logging.logback.LogbackModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        JsonModule,
        UndertowHttpServerModule,
        CamundaEngineModule,
        CamundaRestUndertowModule,
        JdbcDatabaseModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
