package io.koraframework.guide.cache;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.cache.caffeine.CaffeineCacheModule;
import io.koraframework.cache.redis.lettuce.LettuceRedisCacheModule;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule;
import io.koraframework.json.common.JsonModule;
import io.koraframework.logging.logback.LogbackModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        JsonModule,
        LogbackModule,
        UndertowPublicHttpServerModule,
        CaffeineCacheModule,
        LettuceRedisCacheModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
