package io.koraframework.example.kafka.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.kafka.ConnectionKafka;
import io.goodforgod.testcontainers.extensions.kafka.Event;
import io.goodforgod.testcontainers.extensions.kafka.KafkaConnection;
import io.goodforgod.testcontainers.extensions.kafka.TestcontainersKafka;
import io.goodforgod.testcontainers.extensions.kafka.Topics;
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
class AutoCommitValueExceptionListenerTests implements KoraAppTestConfigModifier {

    @ConnectionKafka
    private KafkaConnection connection;

    @Tag(AutoCommitValueExceptionListenerModule.AutoCommitValueExceptionListenerProcessTag.class)
    @TestComponent
    private Lifecycle consumerLifecycle;

    @TestComponent
    private AutoCommitValueExceptionListener consumer;

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
        var event = new JSONObject().put("username", "Ivan").put("code", 1);

        // when
        connection.send(topic, Event.ofValueAndRandomKey(event));

        // then
        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollExecutorService(Executors.newSingleThreadExecutor())
                .until(() -> consumer.received().size() == 1);
    }

    @Test
    void failed() {
        // given
        var topic = "my-topic-consumer";
        var event = "incorrect";

        // when
        connection.send(topic, Event.ofValueAndRandomKey(event));

        // then
        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollExecutorService(Executors.newSingleThreadExecutor())
                .until(() -> consumer.failed().size() == 1);
        assertEquals(0, consumer.received().size());
    }
}
