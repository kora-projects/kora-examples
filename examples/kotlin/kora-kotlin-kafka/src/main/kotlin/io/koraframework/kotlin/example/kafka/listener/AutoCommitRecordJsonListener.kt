package io.koraframework.kotlin.example.kafka.listener

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.Deserializer
import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Tag
import io.koraframework.json.common.JsonReader
import io.koraframework.json.common.annotation.Json
import io.koraframework.kafka.common.annotation.KafkaListener
import io.koraframework.kafka.common.consumer.telemetry.KafkaConsumerTelemetry

@Component
class AutoCommitRecordJsonListener : AbstractListener<AutoCommitRecordJsonListener.MyEvent>() {
    @Json
    data class MyEvent(val username: String, val code: Int)

    @KafkaListener("kafka.consumer.my-listener")
    fun process(record: ConsumerRecord<String, @Json MyEvent>) {
        success(record.value())
    }
}

