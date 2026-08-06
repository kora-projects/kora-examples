package io.koraframework.example.graalvm.kafka;

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
class BlackBoxTests {

    private static final AppContainer container = AppContainer.build()
            .withNetwork(org.testcontainers.containers.Network.SHARED);

    @ConnectionKafka
    private KafkaConnection connection;

    @BeforeAll
    public static void setup(@ConnectionKafka KafkaConnection connection) {
        var params = connection.paramsInNetwork().orElseThrow();
        container.withEnv(Map.of(
                // the broker advertises :9092 to the host and its BROKER listener on :9093 inside
                // the network, so a sibling container has to bootstrap on the latter - otherwise it
                // connects and then receives metadata pointing back at localhost
                "KAFKA_BOOTSTRAP", params.bootstrapServers().replace(":9092", ":9093"),
                "LOGGING_LEVEL_KORA", "INFO",
                "LOGGING_LEVEL_APP", "DEBUG",
                "LOGGING_LEVEL_KORA_KAFKA", "TRACE"));
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
