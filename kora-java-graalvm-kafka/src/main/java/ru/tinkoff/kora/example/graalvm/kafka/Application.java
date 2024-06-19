package ru.tinkoff.kora.example.graalvm.kafka;

import io.goodforgod.graalvm.hint.annotation.NativeImageHint;
import ru.tinkoff.kora.application.graph.KoraApplication;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.config.yaml.YamlConfigModule;
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule;
import ru.tinkoff.kora.json.module.JsonModule;
import ru.tinkoff.kora.kafka.common.KafkaModule;
import ru.tinkoff.kora.logging.logback.LogbackModule;
import ru.tinkoff.kora.micrometer.module.MetricsModule;

@NativeImageHint(name = "application", entrypoint = Application.class)
@KoraApp
public interface Application extends
        YamlConfigModule,
        LogbackModule,
        JsonModule,
        UndertowHttpServerModule,
        KafkaModule,
        MetricsModule {
    // TODO change UndertowHttpServerModule -> UndertowModule in next versions

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
