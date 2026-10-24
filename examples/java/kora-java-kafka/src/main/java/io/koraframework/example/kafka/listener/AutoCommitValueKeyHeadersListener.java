package io.koraframework.example.kafka.listener;

import org.apache.kafka.common.header.Headers;
import io.koraframework.common.annotation.Component;
import io.koraframework.kafka.common.annotation.KafkaListener;

@Component
public final class AutoCommitValueKeyHeadersListener extends AbstractListener<String> {

    @KafkaListener("kafka.consumer.my-listener")
    void process(String key, String event, Headers headers) {
        success(event);
    }
}
