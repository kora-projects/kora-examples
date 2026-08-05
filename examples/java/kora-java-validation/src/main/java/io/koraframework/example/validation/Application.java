package io.koraframework.example.validation;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.logging.logback.LogbackModule;
import io.koraframework.validation.common.constraint.ValidatorModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        ValidatorModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
