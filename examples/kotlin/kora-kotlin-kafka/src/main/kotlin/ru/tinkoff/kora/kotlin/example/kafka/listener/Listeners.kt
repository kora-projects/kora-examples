package ru.tinkoff.kora.kotlin.example.kafka.listener

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.common.header.Headers
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.kafka.common.annotation.KafkaListener

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
class AutoCommitValueExceptionListener : AbstractListener<String>() {
    @KafkaListener("kafka.consumer.my-listener")
    fun process(value: String) {
        fail(IllegalStateException(value))
    }
}
