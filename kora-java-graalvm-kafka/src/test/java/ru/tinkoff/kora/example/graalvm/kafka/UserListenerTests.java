package ru.tinkoff.kora.example.graalvm.kafka;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.Network;
import io.goodforgod.testcontainers.extensions.kafka.*;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@TestcontainersKafka(
        network = @Network(shared = true),
        mode = ContainerMode.PER_RUN,
        topics = @Topics({ "tasks", "users" }))
class UserListenerTests {

    private static final AppContainer container = AppContainer.build()
            .withNetwork(org.testcontainers.containers.Network.SHARED);

    @ContainerKafkaConnection
    private KafkaConnection connection;

    @BeforeAll
    public static void setup(@ContainerKafkaConnection KafkaConnection connection) {
        var params = connection.paramsInNetwork().orElseThrow();
        container.withEnv(Map.of("KAFKA_BOOTSTRAP", params.bootstrapServers()));
        container.start();
    }

    @Test
    void userEventReceivedAndTaskEventSent() {
        // given
        var topicUsers = "users";
        var topicTasks = "tasks";
        var event = new JSONObject().put("id", UUID.randomUUID().toString()).put("name", "Ivan");

        // when
        var consumerTask = connection.subscribe(topicTasks);
        connection.send(topicUsers, Event.ofValueAndRandomKey(event));

        // then
        consumerTask.assertReceivedEqualsInTime(1, Duration.ofSeconds(20));
    }
}
