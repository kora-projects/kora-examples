package ru.tinkoff.kora.example.kafka.listener;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.kafka.common.annotation.KafkaListener;

@Component
public final class AutoCommitValueKeyListener extends AbstractListener<String> {

    @KafkaListener("kafka.consumer.my-listener")
    void process(String key, String value) {
        success(value);
    }
}
