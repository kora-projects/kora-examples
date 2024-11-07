package ru.tinkoff.kora.example.kafka.publisher;

import ru.tinkoff.kora.json.common.annotation.Json;
import ru.tinkoff.kora.kafka.common.annotation.KafkaPublisher;
import ru.tinkoff.kora.kafka.common.annotation.KafkaPublisher.Topic;

@KafkaPublisher("kafka.producer.my-publisher")
public interface TopicJsonPublisher {

    @Json
    record MyEvent(String username, int code) {}

    @Topic("kafka.producer.my-topic")
    void send(@Json MyEvent value);
}
