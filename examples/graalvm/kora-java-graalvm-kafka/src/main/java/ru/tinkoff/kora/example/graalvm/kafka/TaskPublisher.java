package ru.tinkoff.kora.example.graalvm.kafka;

import ru.tinkoff.kora.json.common.annotation.Json;
import ru.tinkoff.kora.kafka.common.annotation.KafkaPublisher;
import ru.tinkoff.kora.kafka.common.annotation.KafkaPublisher.Topic;

@KafkaPublisher("kafka.publisher.task")
public interface TaskPublisher {

    @Json
    record Task(String name, long code) {}

    @Topic("kafka.publisher.task")
    void send(@Json TaskPublisher.Task value);
}
