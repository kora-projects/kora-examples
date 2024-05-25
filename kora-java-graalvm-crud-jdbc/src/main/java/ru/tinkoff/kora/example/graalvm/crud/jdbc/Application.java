package ru.tinkoff.kora.example.graalvm.crud.jdbc;

import io.goodforgod.graalvm.hint.annotation.NativeImageHint;
import io.goodforgod.graalvm.hint.annotation.ResourceHint;
import ru.tinkoff.kora.application.graph.KoraApplication;
import ru.tinkoff.kora.cache.caffeine.CaffeineCacheModule;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.config.hocon.HoconConfigModule;
import ru.tinkoff.kora.database.jdbc.JdbcDatabaseModule;
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule;
import ru.tinkoff.kora.json.module.JsonModule;
import ru.tinkoff.kora.logging.logback.LogbackModule;
import ru.tinkoff.kora.micrometer.module.MetricsModule;
import ru.tinkoff.kora.openapi.management.OpenApiManagementModule;
import ru.tinkoff.kora.resilient.ResilientModule;
import ru.tinkoff.kora.validation.module.ValidationModule;

@ResourceHint(include = { "openapi/http-server.yaml" })
@NativeImageHint(name = "application", entrypoint = Application.class)
@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        JdbcDatabaseModule,
        ValidationModule,
        JsonModule,
        CaffeineCacheModule,
        ResilientModule,
        MetricsModule,
        OpenApiManagementModule,
        UndertowHttpServerModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
