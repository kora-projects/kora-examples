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
class AutoCommitValueListener : AbstractListener<String>() {
    @KafkaListener("kafka.consumer.my-listener")
    fun process(value: String) {
        success(value)
    }
}

@Component
class AutoCommitValueKeyListener : AbstractListener<String>() {
    @KafkaListener("kafka.consumer.my-listener")
    fun process(key: String, event: String) {
        success(event)
    }
}

@Component
class AutoCommitValueKeyHeadersListener : AbstractListener<String>() {
    @KafkaListener("kafka.consumer.my-listener")
    fun process(key: String, event: String, headers: Headers) {
        success(event)
    }
}

@Component
class AutoCommitRecordListener : AbstractListener<String>() {
    @KafkaListener("kafka.consumer.my-listener")
    fun process(record: ConsumerRecord<String, String>) {
        success(record.value())
    }
}

@Component
class AutoCommitRecordsListener : AbstractListener<String>() {
    @KafkaListener("kafka.consumer.my-listener")
    fun process(records: ConsumerRecords<String, String>) {
        records.forEach { success(it.value()) }
    }
}

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

@Component
class AutoCommitValueJsonListener : AbstractListener<AutoCommitValueJsonListener.MyEvent>() {
    @Json
    data class MyEvent(val username: String, val code: Int)

    @KafkaListener("kafka.consumer.my-listener")
    fun process(@Json value: MyEvent) {
        success(value)
    }
}

@Component
class AutoCommitRecordJsonListener : AbstractListener<AutoCommitRecordJsonListener.MyEvent>() {
    @Json
    data class MyEvent(val username: String, val code: Int)

    @KafkaListener("kafka.consumer.my-listener")
    fun process(record: ConsumerRecord<String, @Json MyEvent>) {
        success(record.value())
    }
}

@Component
class AutoCommitValueMapperListener : AbstractListener<AutoCommitValueMapperListener.MyEvent>() {
    @Json
    data class MyEvent(val username: String, val code: Int)

    @Tag(MyEvent::class)
    @Component
    class MyDeserializer(private val reader: JsonReader<MyEvent>) : Deserializer<MyEvent> {
        override fun deserialize(topic: String, data: ByteArray): MyEvent {
            return reader.read(data)
        }
    }

    @KafkaListener("kafka.consumer.my-listener")
    fun process(@Tag(MyEvent::class) value: MyEvent) {
        success(value)
    }
}

@Component
class AutoCommitRecordMapperListener : AbstractListener<AutoCommitRecordMapperListener.MyEvent>() {
    @Json
    data class MyEvent(val username: String, val code: Int)

    @Tag(MyEvent::class)
    @Component
    class MyDeserializer(private val reader: JsonReader<MyEvent>) : Deserializer<MyEvent> {
        override fun deserialize(topic: String, data: ByteArray): MyEvent {
            return reader.read(data)
        }
    }

    @KafkaListener("kafka.consumer.my-listener")
    fun process(record: ConsumerRecord<String, @Tag(MyEvent::class) MyEvent>) {
        success(record.value())
    }
}

@Component
class AutoCommitValueExceptionListener : AbstractListener<AutoCommitValueExceptionListener.MyEvent>() {
    @Json
    data class MyEvent(val username: String, val code: Int)

    @KafkaListener("kafka.consumer.my-listener")
    fun process(@Json value: MyEvent?, exception: Exception?) {
        if (exception == null) {
            success(value!!)
        } else {
            fail(exception)
        }
    }
}

@Component
class AutoCommitRecordExceptionListener : AbstractListener<AutoCommitRecordExceptionListener.MyEvent>() {
    @Json
    data class MyEvent(val username: String, val code: Int)

    @KafkaListener("kafka.consumer.my-listener")
    fun process(record: ConsumerRecord<String, @Json MyEvent>?, exception: Exception?) {
        if (exception == null) {
            success(record!!.value())
        } else {
            fail(exception)
        }
    }
}

@Component
class ManualCommitRecordListener : AbstractListener<String>() {
    @KafkaListener("kafka.consumer.my-listener")
    fun process(record: ConsumerRecord<String, String>, consumer: Consumer<String, String>) {
        success(record.value())
        consumer.commitSync()
    }
}
