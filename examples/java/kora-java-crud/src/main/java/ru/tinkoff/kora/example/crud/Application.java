package ru.tinkoff.kora.example.crud;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.cache.caffeine.CaffeineCacheModule;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.database.jdbc.JdbcDatabaseModule;
import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule;
import io.koraframework.json.common.JsonModule;
import io.koraframework.logging.logback.LogbackModule;
import io.koraframework.micrometer.module.MetricsModule;
import io.koraframework.openapi.management.OpenApiManagementModule;
import io.koraframework.resilient.ResilientModule;
import io.koraframework.validation.module.ValidationModule;

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
        UndertowPublicHttpServerModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
