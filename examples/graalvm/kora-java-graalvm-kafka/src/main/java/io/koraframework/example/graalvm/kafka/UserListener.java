package io.koraframework.example.graalvm.kafka;

import io.koraframework.common.annotation.Component;
import io.koraframework.json.common.annotation.Json;
import io.koraframework.kafka.common.annotation.KafkaListener;

@Component
public final class UserListener {

    @Json
    public record User(String id, String name) {}

    private final TaskPublisher taskPublisher;

    public UserListener(TaskPublisher taskPublisher) {
        this.taskPublisher = taskPublisher;
    }

    @KafkaListener("kafka.listener.user")
    void process(@Json UserListener.User value) {
        long code = System.currentTimeMillis();
        taskPublisher.send(new TaskPublisher.Task(value.name() + "-" + code, code));
    }
}
