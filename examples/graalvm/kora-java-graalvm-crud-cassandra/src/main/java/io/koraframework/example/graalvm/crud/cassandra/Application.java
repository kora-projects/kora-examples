package io.koraframework.example.graalvm.crud.cassandra;

import io.goodforgod.graalvm.hint.annotation.NativeImageHint;
import io.goodforgod.graalvm.hint.annotation.ReflectionHint;
import io.goodforgod.graalvm.hint.annotation.ResourceHint;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.koraframework.application.graph.KoraApplication;
import io.koraframework.cache.redis.lettuce.LettuceRedisCacheModule;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.database.cassandra.CassandraDatabaseModule;
import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule;
import io.koraframework.json.common.JsonModule;
import io.koraframework.logging.logback.LogbackModule;
import io.koraframework.micrometer.module.MetricsModule;
import io.koraframework.openapi.management.OpenApiManagementModule;
import io.koraframework.resilient.ResilientModule;
import io.koraframework.validation.module.ValidationModule;

@ResourceHint(include = { "openapi/http-server.yaml" })
@ReflectionHint(types = NioDatagramChannel.class)
@NativeImageHint(name = "application", entrypoint = Application.class)
@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        CassandraDatabaseModule,
        ValidationModule,
        JsonModule,
        LettuceRedisCacheModule,
        ResilientModule,
        MetricsModule,
        OpenApiManagementModule,
        UndertowPublicHttpServerModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
