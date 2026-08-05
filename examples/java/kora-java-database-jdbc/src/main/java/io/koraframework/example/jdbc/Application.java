package io.koraframework.example.jdbc;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.common.annotation.Tag;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.database.jdbc.JdbcDatabase;
import io.koraframework.database.jdbc.JdbcDatabaseModule;
import io.koraframework.json.common.JsonCommonModule;
import io.koraframework.logging.logback.LogbackModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        JsonCommonModule,
        JdbcDatabaseModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }

    // Also when using UndertowHttpServer, you can use XnioWorker as Executor
    @Tag(JdbcDatabase.class)
    default Executor jdbcExecutor() {
        return Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors(), 2) * 2);
    }
}
