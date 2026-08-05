package io.koraframework.example.graalvm.kafka;

import io.koraframework.json.common.annotation.Json;
import io.koraframework.kafka.common.annotation.KafkaPublisher;
import io.koraframework.kafka.common.annotation.KafkaPublisher.Topic;

@KafkaPublisher("kafka.publisher.task")
public interface TaskPublisher {

    @Json
    record Task(String name, long code) {}

    @Topic("kafka.publisher.task")
    void send(@Json TaskPublisher.Task value);
}
