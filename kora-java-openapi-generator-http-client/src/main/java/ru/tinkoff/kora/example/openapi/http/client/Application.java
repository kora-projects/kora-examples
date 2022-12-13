package ru.tinkoff.kora.example.openapi.http.client;

import ru.tinkoff.kora.application.graph.KoraApplication;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.config.hocon.HoconConfigModule;
import ru.tinkoff.kora.http.client.jdk.JdkHttpClientModule;
import ru.tinkoff.kora.json.module.JsonModule;
import ru.tinkoff.kora.logging.logback.LogbackModule;
import ru.tinkoff.kora.validation.module.ValidationModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        ValidationModule,
        JsonModule,
        JdkHttpClientModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
