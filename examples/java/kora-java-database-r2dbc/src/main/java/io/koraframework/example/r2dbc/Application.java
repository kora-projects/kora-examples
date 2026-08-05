package io.koraframework.example.r2dbc;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.database.r2dbc.R2dbcDatabaseModule;
import io.koraframework.logging.logback.LogbackModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        R2dbcDatabaseModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
