package io.koraframework.example.soap.client;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.http.client.apache.ApacheHttpClientModule;
import io.koraframework.logging.logback.LogbackModule;
import io.koraframework.soap.client.common.SoapClientModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        ApacheHttpClientModule,
        SoapClientModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
