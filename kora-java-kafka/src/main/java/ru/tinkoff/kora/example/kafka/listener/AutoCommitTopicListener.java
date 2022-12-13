package ru.tinkoff.kora.example.kafka.listener;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.kafka.common.annotation.KafkaListener;

@Component
public final class AutoCommitTopicListener extends AbstractListener<String> {

    @KafkaListener("kafka.consumer.my-listener")
    void process(String value) {
        success(value);
    }
}
