package io.koraframework.kotlin.example.kafka

import io.goodforgod.testcontainers.extensions.kafka.KafkaConnection
import io.koraframework.test.extension.junit5.KoraConfigModification

fun kafkaConfig(connection: KafkaConnection): KoraConfigModification {
    return KoraConfigModification.ofSystemProperty("KAFKA_BOOTSTRAP", connection.params().bootstrapServers())
}
