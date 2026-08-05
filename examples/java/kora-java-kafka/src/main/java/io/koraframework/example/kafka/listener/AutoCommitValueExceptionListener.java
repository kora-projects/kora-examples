package io.koraframework.example.kafka.listener;

import static io.koraframework.example.kafka.listener.AutoCommitValueExceptionListener.MyEvent;

import org.jspecify.annotations.Nullable;
import io.koraframework.common.annotation.Component;
import io.koraframework.json.common.annotation.Json;
import io.koraframework.kafka.common.annotation.KafkaListener;

@Component
public final class AutoCommitValueExceptionListener extends AbstractListener<MyEvent> {

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
