package ru.tinkoff.kora.example.kafka.publisher;

import org.apache.kafka.common.header.Headers;
import ru.tinkoff.kora.kafka.common.annotation.KafkaPublisher;
import ru.tinkoff.kora.kafka.common.annotation.KafkaPublisher.Topic;

@KafkaPublisher("kafka.producer.my-publisher")
public interface TopicKeyHeadersPublisher {

    @Topic("kafka.producer.my-topic")
    void send(String key, String value, Headers headers);
}
