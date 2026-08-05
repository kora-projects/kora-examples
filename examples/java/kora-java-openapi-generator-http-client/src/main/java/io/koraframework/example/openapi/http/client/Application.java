package io.koraframework.example.openapi.http.client;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.http.client.jdk.JdkHttpClientModule;
import io.koraframework.json.common.JsonModule;
import io.koraframework.logging.logback.LogbackModule;
import io.koraframework.validation.module.ValidationModule;

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
