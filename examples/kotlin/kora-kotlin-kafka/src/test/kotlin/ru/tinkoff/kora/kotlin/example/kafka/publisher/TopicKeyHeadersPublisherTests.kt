package ru.tinkoff.kora.kotlin.example.kafka.publisher

import io.goodforgod.testcontainers.extensions.ContainerMode
import io.goodforgod.testcontainers.extensions.kafka.ConnectionKafka
import io.goodforgod.testcontainers.extensions.kafka.KafkaConnection
import io.goodforgod.testcontainers.extensions.kafka.TestcontainersKafka
import io.goodforgod.testcontainers.extensions.kafka.Topics
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.header.internals.RecordHeaders
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.kotlin.example.kafka.Application
import ru.tinkoff.kora.kotlin.example.kafka.kafkaConfig
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent
import java.nio.charset.StandardCharsets

@TestcontainersKafka(mode = ContainerMode.PER_RUN, topics = Topics("my-topic-producer"))
@KoraAppTest(Application::class)
class TopicKeyHeadersPublisherTests : KoraAppTestConfigModifier {
    @ConnectionKafka
    lateinit var connection: KafkaConnection

    @TestComponent
    lateinit var publisher: TopicKeyHeadersPublisher
    override fun config(): KoraConfigModification = kafkaConfig(connection)

    @Test
    fun processed() {
        val consumer = connection.subscribe("my-topic-producer")
        val headers = RecordHeaders(listOf(RecordHeader("2", "3".toByteArray(StandardCharsets.UTF_8))))

        publisher.send("1", """{"username":"Ivan","code":1}""", headers)

        val receivedEvent = consumer.assertReceivedAtLeast(1)[0]
        assertEquals("1", receivedEvent.key().asString())
        assertEquals("Ivan", receivedEvent.value().asJson().getString("username"))
        assertEquals(1, receivedEvent.value().asJson().getInt("code"))
        assertEquals(1, receivedEvent.headers().size)
        assertEquals("2", receivedEvent.headers()[0].key())
        assertEquals("3", receivedEvent.headers()[0].value().asString())
    }
}
