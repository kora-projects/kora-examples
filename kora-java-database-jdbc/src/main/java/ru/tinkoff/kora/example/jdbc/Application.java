package ru.tinkoff.kora.example.jdbc;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import ru.tinkoff.kora.application.graph.KoraApplication;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.config.hocon.HoconConfigModule;
import ru.tinkoff.kora.database.jdbc.JdbcDatabase;
import ru.tinkoff.kora.database.jdbc.JdbcDatabaseModule;
import ru.tinkoff.kora.json.common.JsonCommonModule;
import ru.tinkoff.kora.logging.logback.LogbackModule;

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
