package ru.tinkoff.kora.example.graalvm.kafka;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.json.common.annotation.Json;
import ru.tinkoff.kora.kafka.common.annotation.KafkaListener;

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
