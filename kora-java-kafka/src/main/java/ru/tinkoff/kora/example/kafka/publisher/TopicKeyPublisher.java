package ru.tinkoff.kora.example.kafka.publisher;

import ru.tinkoff.kora.kafka.common.annotation.KafkaPublisher;
import ru.tinkoff.kora.kafka.common.annotation.KafkaPublisher.Topic;

@KafkaPublisher("kafka.producer.my-publisher")
public interface TopicKeyPublisher {

    @Topic("kafka.producer.my-topic")
    void send(String key, String value);
}
