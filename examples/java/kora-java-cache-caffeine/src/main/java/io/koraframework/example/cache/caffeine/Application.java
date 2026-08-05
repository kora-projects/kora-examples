package io.koraframework.example.cache.caffeine;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.cache.caffeine.CaffeineCacheModule;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.logging.logback.LogbackModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        CaffeineCacheModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
