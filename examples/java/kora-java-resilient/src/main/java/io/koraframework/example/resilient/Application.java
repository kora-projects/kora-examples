package io.koraframework.example.resilient;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.logging.logback.LogbackModule;
import io.koraframework.resilient.ResilientModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        ResilientModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
