package io.koraframework.example.kafka.publisher;

import io.koraframework.kafka.common.annotation.KafkaPublisher;
import io.koraframework.kafka.common.annotation.KafkaPublisher.Topic;

@KafkaPublisher("kafka.producer.my-publisher")
public interface TopicKeyPublisher {

    @Topic("kafka.producer.my-topic")
    void send(String key, String value);
}
