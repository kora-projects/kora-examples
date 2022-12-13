package ru.tinkoff.kora.example.kafka.listener;

import org.apache.kafka.common.header.Headers;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.kafka.common.annotation.KafkaListener;

@Component
public final class AutoCommitTopicKeyHeadersListener extends AbstractListener<String> {

    @KafkaListener("kafka.consumer.my-listener")
    void process(String key, String event, Headers headers) {
        success(event);
    }
}
