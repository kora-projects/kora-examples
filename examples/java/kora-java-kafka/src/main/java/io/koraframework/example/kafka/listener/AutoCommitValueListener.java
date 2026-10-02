package io.koraframework.example.kafka.listener;

import io.koraframework.common.annotation.Component;
import io.koraframework.kafka.common.annotation.KafkaListener;

@Component
public final class AutoCommitValueListener extends AbstractListener<String> {

    @KafkaListener("kafka.consumer.my-listener")
    void process(String value) {
        success(value);
    }
}
