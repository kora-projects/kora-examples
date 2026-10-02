package io.koraframework.kotlin.example.kafka.publisher

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.kafka.ConnectionKafka
import io.goodforgod.testcontainers.extensions.kafka.KafkaConnection
import io.goodforgod.testcontainers.extensions.kafka.TestcontainersKafka
import io.goodforgod.testcontainers.extensions.kafka.Topics
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import io.koraframework.kafka.common.producer.TransactionalPublisher.TransactionalConsumer
import io.koraframework.kotlin.example.kafka.Application
import io.koraframework.kotlin.example.kafka.kafkaConfig
import io.koraframework.test.extension.junit5.KoraAppTest
import io.koraframework.test.extension.junit5.KoraAppTestConfigModifier
import io.koraframework.test.extension.junit5.KoraConfigModification
import io.koraframework.test.extension.junit5.TestComponent
import java.time.Duration

@TestcontainersKafka(mode = ContainerMode.PER_RUN, topics = Topics("my-topic-producer"))
@KoraAppTest(Application::class)
class TransactionalPublisherTests : KoraAppTestConfigModifier {
    @ConnectionKafka(properties = [ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed"])
    lateinit var connection: KafkaConnection

    @TestComponent
    lateinit var publisher: MyTransactionalPublisher
    override fun config(): KoraConfigModification = kafkaConfig(connection)

    @Test
    fun committed() {
        val consumer = connection.subscribe("my-topic-producer")

        publisher.inTx(TransactionalConsumer<MyTransactionalPublisher.TopicPublisher, RuntimeException> { producer ->
            producer.send("""{"username":"Foo","code":1}""")
            producer.send("""{"username":"Bar","code":2}""")
        })

        val receivedEvents = consumer.assertReceivedAtLeast(2)
        receivedEvents.forEach { assertNull(it.key()) }
        assertTrue(receivedEvents.any { it.value().asJson().getString("username") == "Foo" })
        assertTrue(receivedEvents.any { it.value().asJson().getString("username") == "Bar" })
        assertTrue(receivedEvents.any { it.value().asJson().getInt("code") == 1 })
        assertTrue(receivedEvents.any { it.value().asJson().getInt("code") == 2 })
    }

    @Test
    fun aborted() {
        val consumer = connection.subscribe("my-topic-producer")

        assertThrows(IllegalStateException::class.java) {
            publisher.inTx(TransactionalConsumer<MyTransactionalPublisher.TopicPublisher, IllegalStateException> { producer ->
                producer.send("""{"username":"Foo","code":1}""")
                producer.send("""{"username":"Bar","code":2}""")
                throw IllegalStateException("Ops something happened")
            })
        }

        consumer.assertReceivedNone(Duration.ofSeconds(5))
    }
}
