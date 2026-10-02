package io.koraframework.example.kafka;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.json.common.JsonModule;
import io.koraframework.kafka.common.KafkaModule;
import io.koraframework.logging.logback.LogbackModule;
import io.koraframework.resilient.ResilientModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        JsonModule,
        ResilientModule,
        KafkaModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
