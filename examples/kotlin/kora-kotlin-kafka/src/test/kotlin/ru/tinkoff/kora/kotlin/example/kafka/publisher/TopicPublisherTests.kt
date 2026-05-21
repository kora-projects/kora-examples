package ru.tinkoff.kora.kotlin.example.kafka.publisher

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.kafka.ConnectionKafka
import io.goodforgod.testcontainers.extensions.kafka.KafkaConnection
import io.goodforgod.testcontainers.extensions.kafka.TestcontainersKafka
import io.goodforgod.testcontainers.extensions.kafka.Topics
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.kotlin.example.kafka.Application
import ru.tinkoff.kora.kotlin.example.kafka.kafkaConfig
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent

@TestcontainersKafka(mode = ContainerMode.PER_RUN, topics = Topics("my-topic-producer"))
@KoraAppTest(Application::class)
class TopicPublisherTests : KoraAppTestConfigModifier {
    @ConnectionKafka
    lateinit var connection: KafkaConnection

    @TestComponent
    lateinit var publisher: TopicPublisher
    override fun config(): KoraConfigModification = kafkaConfig(connection)

    @Test
    fun processed() {
        val consumer = connection.subscribe("my-topic-producer")
        publisher.send("Ivan")

        val receivedEvent = consumer.assertReceivedAtLeast(1)[0]
        assertNull(receivedEvent.key())
        assertEquals("Ivan", receivedEvent.value().asString())
    }
}
