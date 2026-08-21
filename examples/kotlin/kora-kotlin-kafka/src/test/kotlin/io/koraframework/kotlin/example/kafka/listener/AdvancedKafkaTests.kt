package io.koraframework.kotlin.example.kafka.listener

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.kafka.ConnectionKafka
import io.goodforgod.testcontainers.extensions.kafka.Event
import io.goodforgod.testcontainers.extensions.kafka.KafkaConnection
import io.goodforgod.testcontainers.extensions.kafka.TestcontainersKafka
import io.goodforgod.testcontainers.extensions.kafka.Topics
import io.koraframework.application.graph.Lifecycle
import io.koraframework.common.annotation.Tag
import io.koraframework.kafka.common.exceptions.RecordKeyDeserializationException
import io.koraframework.kafka.common.exceptions.RecordValueDeserializationException
import io.koraframework.kotlin.example.kafka.Application
import io.koraframework.kotlin.example.kafka.kafkaConfig
import io.koraframework.test.extension.junit5.KoraAppTest
import io.koraframework.test.extension.junit5.KoraAppTestConfigModifier
import io.koraframework.test.extension.junit5.KoraConfigModification
import io.koraframework.test.extension.junit5.TestComponent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.testcontainers.shaded.org.awaitility.Awaitility
import java.time.Duration

@TestcontainersKafka(
    mode = ContainerMode.PER_RUN,
    topics = Topics("skip-topic", "malformed-topic", "retry-topic", "transactional-input", "my-topic-producer")
)
@KoraAppTest(Application::class)
class AdvancedKafkaTests : KoraAppTestConfigModifier {
    @ConnectionKafka(properties = [ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed"])
    lateinit var connection: KafkaConnection

    @Tag(SkipRecordListenerModule.SkipRecordListenerProcessTag::class)
    @TestComponent lateinit var skipLifecycle: Lifecycle
    @Tag(MalformedRecordListenerModule.MalformedRecordListenerProcessTag::class)
    @TestComponent lateinit var malformedLifecycle: Lifecycle
    @Tag(RetryListenerModule.RetryListenerProcessTag::class)
    @TestComponent lateinit var retryLifecycle: Lifecycle
    @Tag(TransactionalPipelineListenerModule.TransactionalPipelineListenerProcessTag::class)
    @TestComponent lateinit var transactionalLifecycle: Lifecycle

    @TestComponent lateinit var skipListener: SkipRecordListener
    @TestComponent lateinit var malformedListener: MalformedRecordListener
    @TestComponent lateinit var retryListener: RetryListener

    override fun config(): KoraConfigModification = kafkaConfig(connection)

    @Test
    fun coversSkipMalformedRetryAndTransactionalPipeline() {
        connection.send("skip-topic", Event.ofValue("skip:unsupported"))
        connection.send("skip-topic", Event.ofValue("accepted"))

        val validKey = "{\"id\":\"key\"}"
        val validValue = "{\"message\":\"ok\"}"
        connection.send("malformed-topic", Event.builder().withKey("not-json").withValue(validValue).build())
        connection.send("malformed-topic", Event.builder().withKey(validKey).withValue("not-json").build())

        connection.send("retry-topic", Event.ofValue("eventually-processed"))

        val output = connection.subscribe("my-topic-producer")
        connection.send("transactional-input", Event.builder().withKey("key").withValue("payload").build())

        Awaitility.await().atMost(Duration.ofSeconds(20)).untilAsserted {
            assertEquals(1, skipListener.skipped.get())
            assertTrue("accepted" in skipListener.processed)
            assertEquals(3, retryListener.attempts.get())
            assertTrue("eventually-processed" in retryListener.processed)
            assertTrue(malformedListener.failures.any { it is RecordKeyDeserializationException })
            assertTrue(malformedListener.failures.any { it is RecordValueDeserializationException })
        }

        assertTrue(output.assertReceivedAtLeast(1).any { it.value().asString() == "processed:payload" })
    }
}
