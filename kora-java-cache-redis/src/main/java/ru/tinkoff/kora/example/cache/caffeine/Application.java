package ru.tinkoff.kora.example.cache.caffeine;

import ru.tinkoff.kora.application.graph.KoraApplication;
import ru.tinkoff.kora.cache.redis.RedisCacheModule;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.config.hocon.HoconConfigModule;
import ru.tinkoff.kora.logging.logback.LogbackModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        RedisCacheModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
