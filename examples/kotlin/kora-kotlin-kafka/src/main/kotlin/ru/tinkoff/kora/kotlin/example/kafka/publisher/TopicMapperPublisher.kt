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

