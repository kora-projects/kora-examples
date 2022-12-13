package ru.tinkoff.kora.example.kafka.listener;

import static ru.tinkoff.kora.example.kafka.listener.AutoCommitTopicExceptionListener.MyEvent;

import jakarta.annotation.Nullable;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.json.common.annotation.Json;
import ru.tinkoff.kora.kafka.common.annotation.KafkaListener;

@Component
public final class AutoCommitTopicExceptionListener extends AbstractListener<MyEvent> {

    @Json
    public record MyEvent(String username, int code) {}

    @KafkaListener("kafka.consumer.my-listener")
    void process(@Json @Nullable MyEvent value, @Nullable Exception exception) {
        if (exception == null) {
            success(value);
        } else {
            fail(exception);
        }
    }
}
