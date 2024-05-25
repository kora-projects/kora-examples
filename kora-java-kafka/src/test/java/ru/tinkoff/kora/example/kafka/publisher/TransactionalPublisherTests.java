package ru.tinkoff.kora.example.kafka.publisher;

import static org.junit.jupiter.api.Assertions.*;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.kafka.*;
import io.goodforgod.testcontainers.extensions.kafka.Topics;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.kafka.clients.consumer.ConsumerConfig;
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
class TransactionalPublisherTests implements KoraAppTestConfigModifier {

    @ConnectionKafka(properties = { ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed" })
    private KafkaConnection connection;

    @TestComponent
    private MyTransactionalPublisher publisher;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification
                .ofSystemProperty("KAFKA_BOOTSTRAP", connection.params().bootstrapServers());
    }

    @Test
    void committed() {
        // given
        var topic = "my-topic-producer";
        var consumer = connection.subscribe(topic);

        // when
        var code1 = ThreadLocalRandom.current().nextInt(1, 100_000);
        var name1 = "Foo";
        var code2 = ThreadLocalRandom.current().nextInt(1, 100_000);
        var name2 = "Bar";

        publisher.inTx(producer -> {
            var event1 = new JSONObject().put("username", name1).put("code", code1);
            var event2 = new JSONObject().put("username", name2).put("code", code2);
            producer.send(event1.toString());
            producer.send(event2.toString());
        });

        // then
        var receivedEvent = consumer.assertReceivedAtLeast(2);
        for (ReceivedEvent event : receivedEvent) {
            assertNull(event.key());
        }

        assertTrue(receivedEvent.stream().anyMatch(e -> name1.equals(e.value().asJson().get("username"))));
        assertTrue(receivedEvent.stream().anyMatch(e -> name2.equals(e.value().asJson().get("username"))));
        assertTrue(receivedEvent.stream().anyMatch(e -> code1 == e.value().asJson().getInt("code")));
        assertTrue(receivedEvent.stream().anyMatch(e -> code2 == e.value().asJson().getInt("code")));
    }

    @Test
    void aborted() {
        // given
        var topic = "my-topic-producer";
        var consumer = connection.subscribe(topic);

        // when
        var code1 = ThreadLocalRandom.current().nextInt(1, 100_000);
        var name1 = "Foo";
        var code2 = ThreadLocalRandom.current().nextInt(1, 100_000);
        var name2 = "Bar";

        assertThrows(IllegalStateException.class, () -> publisher.inTx(producer -> {
            var event1 = new JSONObject().put("username", name1).put("code", code1);
            var event2 = new JSONObject().put("username", name2).put("code", code2);
            producer.send(event1.toString());
            producer.send(event2.toString());

            if (true) {
                throw new IllegalStateException("Ops something happened");
            }
        }));

        // then
        consumer.assertReceivedNone(Duration.ofSeconds(5));
    }
}
