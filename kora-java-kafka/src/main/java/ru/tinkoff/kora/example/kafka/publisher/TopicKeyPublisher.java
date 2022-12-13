package ru.tinkoff.kora.example.kafka.publisher;

import ru.tinkoff.kora.kafka.common.annotation.KafkaPublisher;

@KafkaPublisher("kafka.producer.my-publisher")
public interface TopicKeyPublisher {

    @KafkaPublisher.Topic("kafka.producer.my-topic")
    void send(String key, String value);
}
