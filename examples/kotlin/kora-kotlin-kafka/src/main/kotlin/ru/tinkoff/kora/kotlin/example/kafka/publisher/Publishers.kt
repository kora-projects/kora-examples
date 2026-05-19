package ru.tinkoff.kora.kotlin.example.kafka.publisher

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.kafka.common.annotation.KafkaPublisher
import ru.tinkoff.kora.kafka.common.annotation.KafkaPublisher.Topic
import java.util.concurrent.Future

@KafkaPublisher("kafka.producer.my-publisher")
interface TopicPublisher {
    @Topic("kafka.producer.my-topic")
    fun send(value: String)

    @Topic("kafka.producer.my-topic")
    fun sendMeta(value: String): RecordMetadata

    @Topic("kafka.producer.my-topic")
    fun sendMetaAsync(value: String): Future<RecordMetadata>
}

@KafkaPublisher("kafka.producer.my-publisher")
interface ProducerPublisher {
    fun send(record: ProducerRecord<String, String>)
}

@Root
class RootPublisher(
    private val topicPublisher: TopicPublisher,
    private val producerPublisher: ProducerPublisher,
)
