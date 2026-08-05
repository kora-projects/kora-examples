package io.koraframework.kotlin.example.kafka.publisher

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.Serializer
import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Tag
import io.koraframework.common.annotation.Root
import io.koraframework.json.common.JsonWriter
import io.koraframework.json.common.annotation.Json
import io.koraframework.kafka.common.annotation.KafkaPublisher
import io.koraframework.kafka.common.annotation.KafkaPublisher.Topic
import io.koraframework.kafka.common.producer.TransactionalPublisher
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

