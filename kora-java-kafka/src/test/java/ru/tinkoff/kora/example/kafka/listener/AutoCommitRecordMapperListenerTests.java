package ru.tinkoff.kora.example.kafka.listener;

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
import ru.tinkoff.kora.application.graph.Lifecycle;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.example.kafka.Application;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@TestcontainersKafka(mode = ContainerMode.PER_RUN, topics = @Topics({ "my-topic-consumer" }))
@KoraAppTest(Application.class)
class AutoCommitRecordMapperListenerTests implements KoraAppTestConfigModifier {

    @ConnectionKafka
    private KafkaConnection connection;

    @Tag(AutoCommitRecordMapperListenerModule.AutoCommitRecordMapperListenerProcessTag.class)
    @TestComponent
    private Lifecycle consumerLifecycle;

    @TestComponent
    private AutoCommitRecordMapperListener consumer;

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
}
