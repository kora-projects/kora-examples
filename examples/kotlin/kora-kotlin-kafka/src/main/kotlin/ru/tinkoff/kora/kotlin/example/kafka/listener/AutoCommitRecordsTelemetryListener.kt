package ru.tinkoff.kora.kotlin.example.kafka.listener

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.Deserializer
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.json.common.JsonReader
import ru.tinkoff.kora.json.common.annotation.Json
import ru.tinkoff.kora.kafka.common.annotation.KafkaListener
import ru.tinkoff.kora.kafka.common.consumer.telemetry.KafkaConsumerTelemetry

@Component
class AutoCommitRecordsTelemetryListener : AbstractListener<String>() {
    @KafkaListener("kafka.consumer.my-listener")
    fun process(
        records: ConsumerRecords<String, String>,
        ctx: KafkaConsumerTelemetry.KafkaConsumerRecordsTelemetryContext<String, String>
    ) {
        records.forEach {
            val telemetryContext = ctx.get(it)
            success(it.value())
            telemetryContext.close(null)
        }
    }
}

