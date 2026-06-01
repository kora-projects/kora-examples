package ru.tinkoff.kora.example.kafka.listener;

import static ru.tinkoff.kora.example.kafka.listener.AutoCommitValueJsonListener.MyEvent;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.json.common.annotation.Json;
import ru.tinkoff.kora.kafka.common.annotation.KafkaListener;

@Component
public final class AutoCommitValueJsonListener extends AbstractListener<MyEvent> {

    @Json
    public record MyEvent(String username, int code) {}

    @KafkaListener("kafka.consumer.my-listener")
    void process(@Json MyEvent value) {
        success(value);
    }
}
