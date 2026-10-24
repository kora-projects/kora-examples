package io.koraframework.example.cache.redis;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.cache.redis.lettuce.LettuceRedisCacheModule;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.logging.logback.LogbackModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        LettuceRedisCacheModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
