package io.koraframework.example.kafka.listener;

import io.koraframework.common.annotation.Component;
import io.koraframework.kafka.common.annotation.KafkaListener;

@Component
public final class AutoCommitValueKeyListener extends AbstractListener<String> {

    @KafkaListener("kafka.consumer.my-listener")
    void process(String key, String value) {
        success(value);
    }
}
