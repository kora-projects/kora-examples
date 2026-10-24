package io.koraframework.kotlin.example.kafka.listener

import io.koraframework.common.annotation.Component
import io.koraframework.json.common.annotation.Json
import io.koraframework.kafka.common.annotation.KafkaListener
import io.koraframework.kafka.common.exceptions.KafkaSkipRecordException
import io.koraframework.kotlin.example.kafka.publisher.MyTransactionalPublisher
import io.koraframework.resilient.retry.Retry
import io.koraframework.resilient.retry.annotation.RetrySpec
import io.koraframework.resilient.retry.annotation.Retryable
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

@Component
class SkipRecordListener {
    val processed = CopyOnWriteArrayList<String>()
    val skipped = AtomicInteger()

    @KafkaListener("kafka.consumer.skip-listener")
    fun process(value: String) {
        if (value.startsWith("skip:")) {
            skipped.incrementAndGet()
            throw KafkaSkipRecordException(IllegalArgumentException("Unsupported record: $value"))
        }
        processed += value
    }
}

@Component
class MalformedRecordListener {
    @Json data class Key(val id: String)
    @Json data class Value(val message: String)

    val failures = CopyOnWriteArrayList<Exception>()

    @KafkaListener("kafka.consumer.malformed-listener")
    fun process(@Json key: Key?, @Json value: Value?, exception: Exception?) {
        if (exception != null) failures += exception
    }
}

@RetrySpec("resilient.retry.kafka-listener")
interface KafkaListenerRetry : Retry

@Component
open class RetryListener {
    val attempts = AtomicInteger()
    val processed = CopyOnWriteArrayList<String>()

    @Retryable(KafkaListenerRetry::class)
    @KafkaListener("kafka.consumer.retry-listener")
    open fun process(value: String) {
        if (attempts.incrementAndGet() < 3) throw IllegalStateException("Transient processing failure")
        processed += value
    }
}

@Component
class TransactionalPipelineListener(private val publisher: MyTransactionalPublisher) {
    @KafkaListener("kafka.consumer.transactional-pipeline")
    fun process(record: ConsumerRecord<String, String>, consumer: Consumer<String, String>) {
        publisher.begin().use { transaction ->
            transaction.publisher().send("processed:${record.value()}")
            transaction.sendOffsetsToTransaction(
                mapOf(TopicPartition(record.topic(), record.partition()) to OffsetAndMetadata(record.offset() + 1)),
                consumer.groupMetadata()
            )
        }
    }
}
