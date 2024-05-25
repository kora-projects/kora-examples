package ru.tinkoff.kora.example.graalvm.kafka;

import ru.tinkoff.kora.json.common.annotation.Json;
import ru.tinkoff.kora.kafka.common.annotation.KafkaPublisher;

@KafkaPublisher("kafka.publisher.task")
public interface TaskPublisher {

    @Json
    record Task(String name, long code) {}

    @KafkaPublisher.Topic("kafka.publisher.task")
    void send(@Json TaskPublisher.Task value);
}
