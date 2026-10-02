package io.koraframework.example.kafka.listener;

import static io.koraframework.example.kafka.listener.AutoCommitValueJsonListener.MyEvent;

import io.koraframework.common.annotation.Component;
import io.koraframework.json.common.annotation.Json;
import io.koraframework.kafka.common.annotation.KafkaListener;

@Component
public final class AutoCommitValueJsonListener extends AbstractListener<MyEvent> {

    @Json
    public record MyEvent(String username, int code) {}

    @KafkaListener("kafka.consumer.my-listener")
    void process(@Json MyEvent value) {
        success(value);
    }
}
