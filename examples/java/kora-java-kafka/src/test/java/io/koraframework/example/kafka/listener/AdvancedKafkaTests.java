package io.koraframework.example.kafka.listener;

import io.goodforgod.testcontainers.extensions.ContainerMode;
import io.goodforgod.testcontainers.extensions.kafka.ConnectionKafka;
import io.goodforgod.testcontainers.extensions.kafka.Event;
import io.goodforgod.testcontainers.extensions.kafka.KafkaConnection;
import io.goodforgod.testcontainers.extensions.kafka.TestcontainersKafka;
import io.goodforgod.testcontainers.extensions.kafka.Topics;
import io.koraframework.application.graph.Lifecycle;
import io.koraframework.common.annotation.Tag;
import io.koraframework.example.kafka.Application;
import io.koraframework.kafka.common.exceptions.RecordKeyDeserializationException;
import io.koraframework.kafka.common.exceptions.RecordValueDeserializationException;
import io.koraframework.test.extension.junit5.KoraAppTest;
import io.koraframework.test.extension.junit5.KoraAppTestConfigModifier;
import io.koraframework.test.extension.junit5.KoraConfigModification;
import io.koraframework.test.extension.junit5.TestComponent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestcontainersKafka(
    mode = ContainerMode.PER_RUN,
    topics = @Topics({"skip-topic", "malformed-topic", "retry-topic", "transactional-input", "my-topic-producer"})
)
@KoraAppTest(Application.class)
class AdvancedKafkaTests implements KoraAppTestConfigModifier {
    @ConnectionKafka(properties = {ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed"})
    KafkaConnection connection;

    @Tag(SkipRecordListenerModule.SkipRecordListenerProcessTag.class)
    @TestComponent Lifecycle skipLifecycle;
    @Tag(MalformedRecordListenerModule.MalformedRecordListenerProcessTag.class)
    @TestComponent Lifecycle malformedLifecycle;
    @Tag(RetryListenerModule.RetryListenerProcessTag.class)
    @TestComponent Lifecycle retryLifecycle;
    @Tag(TransactionalPipelineListenerModule.TransactionalPipelineListenerProcessTag.class)
    @TestComponent Lifecycle transactionalLifecycle;

    @TestComponent SkipRecordListener skipListener;
    @TestComponent MalformedRecordListener malformedListener;
    @TestComponent RetryListener retryListener;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofSystemProperty("KAFKA_BOOTSTRAP", connection.params().bootstrapServers());
    }

    @Test
    void coversSkipMalformedRetryAndTransactionalPipeline() {
        connection.send("skip-topic", Event.ofValue("skip:unsupported"));
        connection.send("skip-topic", Event.ofValue("accepted"));

        var validKey = "{\"id\":\"key\"}";
        var validValue = "{\"message\":\"ok\"}";
        connection.send("malformed-topic", Event.builder().withKey("not-json").withValue(validValue).build());
        connection.send("malformed-topic", Event.builder().withKey(validKey).withValue("not-json").build());

        connection.send("retry-topic", Event.ofValue("eventually-processed"));

        var output = connection.subscribe("my-topic-producer");
        connection.send("transactional-input", Event.builder().withKey("key").withValue("payload").build());

        Awaitility.await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            assertEquals(1, skipListener.skipped());
            assertTrue(skipListener.processed().contains("accepted"));
            assertEquals(3, retryListener.attempts());
            assertTrue(retryListener.processed().contains("eventually-processed"));
            assertTrue(malformedListener.failures().stream().anyMatch(RecordKeyDeserializationException.class::isInstance));
            assertTrue(malformedListener.failures().stream().anyMatch(RecordValueDeserializationException.class::isInstance));
        });

        assertTrue(output.assertReceivedAtLeast(1).stream()
            .anyMatch(event -> "processed:payload".equals(event.value().asString())));
    }
}
