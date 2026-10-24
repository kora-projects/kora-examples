package io.koraframework.example.kafka.publisher;

import io.koraframework.json.common.annotation.Json;
import io.koraframework.kafka.common.annotation.KafkaPublisher;
import io.koraframework.kafka.common.annotation.KafkaPublisher.Topic;

@KafkaPublisher("kafka.producer.my-publisher")
public interface TopicJsonPublisher {

    @Json
    record MyEvent(String username, int code) {}

    @Topic("kafka.producer.my-topic")
    void send(@Json MyEvent value);
}
