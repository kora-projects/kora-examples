package ru.tinkoff.kora.kotlin.example.kafka.publisher

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.Serializer
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.json.common.JsonWriter
import ru.tinkoff.kora.json.common.annotation.Json
import ru.tinkoff.kora.kafka.common.annotation.KafkaPublisher
import ru.tinkoff.kora.kafka.common.annotation.KafkaPublisher.Topic
import ru.tinkoff.kora.kafka.common.producer.TransactionalPublisher
import java.util.concurrent.Future

@Root
@Component
class RootPublisher(
    private val topicPublisher: TopicPublisher,
    private val producerPublisher: ProducerPublisher,
    private val topicKeyPublisher: TopicKeyPublisher,
    private val topicKeyHeadersPublisher: TopicKeyHeadersPublisher,
    private val topicJsonPublisher: TopicJsonPublisher,
    private val producerJsonPublisher: ProducerJsonPublisher,
    private val topicMapperPublisher: TopicMapperPublisher,
    private val producerMapperPublisher: ProducerMapperPublisher,
    private val transactionalPublisher: MyTransactionalPublisher,
)

