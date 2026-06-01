package ru.tinkoff.kora.guide.messaging.kafka.kafka

import ru.tinkoff.kora.json.common.annotation.Json
import ru.tinkoff.kora.kafka.common.annotation.KafkaPublisher
import ru.tinkoff.kora.kafka.common.annotation.KafkaPublisher.Topic

@KafkaPublisher("kafka.producer.user-created")
interface UserCreatedPublisher {

    @Topic("kafka.producer.user-created-topic")
    fun send(@Json event: UserCreatedEvent)
}
