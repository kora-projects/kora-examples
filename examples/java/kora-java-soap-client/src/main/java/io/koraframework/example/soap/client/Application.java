package io.koraframework.example.soap.client;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.http.client.jdk.JdkHttpClientModule;
import io.koraframework.logging.logback.LogbackModule;
import io.koraframework.soap.client.common.SoapClientModule;

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
