package ru.tinkoff.kora.example.kafka.publisher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.kafka.ContainerKafkaConnection;
import io.goodforgod.testcontainers.extensions.kafka.KafkaConnection;
import io.goodforgod.testcontainers.extensions.kafka.TestcontainersKafka;
import io.goodforgod.testcontainers.extensions.kafka.Topics;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.example.kafka.Application;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@TestcontainersKafka(mode = ContainerMode.PER_RUN, topics = @Topics({ "my-topic-producer" }))
@KoraAppTest(Application.class)
class TopicKeyHeadersPublisherTests implements KoraAppTestConfigModifier {

    @ContainerKafkaConnection
    private KafkaConnection connection;

    @TestComponent
    private TopicKeyHeadersPublisher publisher;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification
                .ofSystemProperty("KAFKA_BOOTSTRAP", connection.params().bootstrapServers());
    }

    @Test
    void processed() {
        // given
        var topic = "my-topic-producer";
        var consumer = connection.subscribe(topic);

        // when
        var code = ThreadLocalRandom.current().nextInt(1, 100_000);
        var name = "Ivan";
        var event = new JSONObject().put("username", name).put("code", code);
        publisher.send("1", event.toString(),
                new RecordHeaders(List.of(new RecordHeader("2", "3".getBytes(StandardCharsets.UTF_8)))));

        // then
        var receivedEvent = consumer.assertReceivedAtLeast(1).get(0);
        assertEquals("1", receivedEvent.key().asString());
        assertEquals(name, receivedEvent.value().asJson().getString("username"));
        assertEquals(code, receivedEvent.value().asJson().getInt("code"));
        assertEquals(1, receivedEvent.headers().size());
        assertEquals("2", receivedEvent.headers().get(0).key());
        assertEquals("3", receivedEvent.headers().get(0).value().asString());
    }
}
