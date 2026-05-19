package ru.tinkoff.kora.example.soap.client;

import ru.tinkoff.kora.application.graph.KoraApplication;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.config.hocon.HoconConfigModule;
import ru.tinkoff.kora.http.client.jdk.JdkHttpClientModule;
import ru.tinkoff.kora.logging.logback.LogbackModule;
import ru.tinkoff.kora.soap.client.common.SoapClientModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        JdkHttpClientModule,
        SoapClientModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
