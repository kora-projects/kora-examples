package io.koraframework.example.graalvm.kafka;

import io.goodforgod.graalvm.hint.annotation.NativeImageHint;
import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.config.yaml.YamlConfigModule;
import io.koraframework.http.server.undertow.UndertowModule;
import io.koraframework.json.common.JsonModule;
import io.koraframework.kafka.common.KafkaModule;
import io.koraframework.logging.logback.LogbackModule;
import io.koraframework.micrometer.module.MetricsModule;

@NativeImageHint(name = "application", entrypoint = Application.class)
@KoraApp
public interface Application extends
        YamlConfigModule,
        LogbackModule,
        JsonModule,
        UndertowModule,
        KafkaModule,
        MetricsModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
