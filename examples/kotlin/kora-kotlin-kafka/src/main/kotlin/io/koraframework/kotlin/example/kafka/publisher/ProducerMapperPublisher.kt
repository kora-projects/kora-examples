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

