package ru.tinkoff.kora.example.kafka.publisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.kafka.ContainerKafkaConnection;
import io.goodforgod.testcontainers.extensions.kafka.KafkaConnection;
import io.goodforgod.testcontainers.extensions.kafka.TestcontainersKafka;
import io.goodforgod.testcontainers.extensions.kafka.Topics;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.example.kafka.Application;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@TestcontainersKafka(mode = ContainerMode.PER_RUN, topics = @Topics({ "my-topic-producer" }))
@KoraAppTest(Application.class)
class ProducerMapperPublisherTests implements KoraAppTestConfigModifier {

    @ContainerKafkaConnection
    private KafkaConnection connection;

    @TestComponent
    private ProducerMapperPublisher publisher;

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
        var name = "Bob";
        var event = new ProducerMapperPublisher.MyEvent(name, code);
        publisher.send(new ProducerRecord<>(topic, event));

        // then
        var receivedEvent = consumer.assertReceivedAtLeast(1).get(0);
        assertNull(receivedEvent.key());
        assertEquals(name, receivedEvent.value().asJson().getString("username"));
        assertEquals(code, receivedEvent.value().asJson().getInt("code"));
    }
}
