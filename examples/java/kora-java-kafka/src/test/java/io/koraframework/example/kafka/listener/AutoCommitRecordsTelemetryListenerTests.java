package io.koraframework.example.kafka.listener;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.kafka.*;
import java.time.Duration;
import java.util.concurrent.Executors;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import io.koraframework.application.graph.Lifecycle;
import io.koraframework.common.annotation.Tag;
import io.koraframework.example.kafka.Application;
import io.koraframework.test.extension.junit5.KoraAppTest;
import io.koraframework.test.extension.junit5.KoraAppTestConfigModifier;
import io.koraframework.test.extension.junit5.KoraConfigModification;
import io.koraframework.test.extension.junit5.TestComponent;

@TestcontainersKafka(mode = ContainerMode.PER_RUN, topics = @Topics({ "my-topic-consumer" }))
@KoraAppTest(Application.class)
class AutoCommitRecordsTelemetryListenerTests implements KoraAppTestConfigModifier {

    @ConnectionKafka
    private KafkaConnection connection;

    @Tag(AutoCommitRecordsTelemetryListenerModule.AutoCommitRecordsTelemetryListenerProcessTag.class)
    @TestComponent
    private Lifecycle consumerLifecycle;

    @TestComponent
    private AutoCommitRecordsTelemetryListener consumer;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification
                .ofSystemProperty("KAFKA_BOOTSTRAP", connection.params().bootstrapServers());
    }

    @Test
    void processed() {
        // given
        var topic = "my-topic-consumer";
        var event = new JSONObject().put("username", "Bob").put("code", 1);

        // when
        connection.send(topic, Event.ofValueAndRandomKey(event));

        // then
        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollExecutorService(Executors.newSingleThreadExecutor())
                .until(() -> consumer.received().size() == 1);
    }
}
