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

@KafkaPublisher("kafka.producer.my-publisher")
interface TopicKeyPublisher {
    @Topic("kafka.producer.my-topic")
    fun send(key: String, value: String)
}

@KafkaPublisher("kafka.producer.my-publisher")
interface TopicKeyHeadersPublisher {
    @Topic("kafka.producer.my-topic")
    fun send(key: String, value: String, headers: Headers)
}

@KafkaPublisher("kafka.producer.my-publisher")
interface TopicJsonPublisher {
    @Json
    data class MyEvent(val username: String, val code: Int)

    @Topic("kafka.producer.my-topic")
    fun send(@Json value: MyEvent)
}

@KafkaPublisher("kafka.producer.my-publisher")
interface ProducerJsonPublisher {
    @Json
    data class MyEvent(val username: String, val code: Int)

    fun send(record: ProducerRecord<String, @Json MyEvent>)
}

@KafkaPublisher("kafka.producer.my-publisher")
interface TopicMapperPublisher {
    @Json
    data class MyEvent(val username: String, val code: Int)

    @Tag(MyEvent::class)
    @Component
    class MySerializer(private val writer: JsonWriter<MyEvent>) : Serializer<MyEvent> {
        override fun serialize(topic: String, data: MyEvent): ByteArray {
            return writer.toByteArray(data)
        }
    }

    @Topic("kafka.producer.my-topic")
    fun send(@Tag(MyEvent::class) value: MyEvent)
}

@KafkaPublisher("kafka.producer.my-publisher")
interface ProducerMapperPublisher {
    @Json
    data class MyEvent(val username: String, val code: Int)

    @Tag(MyEvent::class)
    @Component
    class MySerializer(private val writer: JsonWriter<MyEvent>) : Serializer<MyEvent> {
        override fun serialize(topic: String, data: MyEvent): ByteArray {
            return writer.toByteArray(data)
        }
    }

    fun send(record: ProducerRecord<String, @Tag(MyEvent::class) MyEvent>)
}

@KafkaPublisher("kafka.producer.my-transactional")
interface MyTransactionalPublisher : TransactionalPublisher<MyTransactionalPublisher.TopicPublisher> {
    @KafkaPublisher("kafka.producer.my-publisher")
    interface TopicPublisher {
        @Topic("kafka.producer.my-topic")
        fun send(value: String)
    }
}

@Root
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
