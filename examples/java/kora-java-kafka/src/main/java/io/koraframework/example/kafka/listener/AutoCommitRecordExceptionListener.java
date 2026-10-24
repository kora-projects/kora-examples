package io.koraframework.example.kafka.listener;

import static io.koraframework.example.kafka.listener.AutoCommitRecordExceptionListener.MyEvent;

import org.jspecify.annotations.Nullable;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import io.koraframework.common.annotation.Component;
import io.koraframework.json.common.annotation.Json;
import io.koraframework.kafka.common.annotation.KafkaListener;

@Component
public final class AutoCommitRecordExceptionListener extends AbstractListener<MyEvent> {

    @Json
    public record MyEvent(String username, int code) {}

    @KafkaListener("kafka.consumer.my-listener")
    void process(@Nullable ConsumerRecord<String, @Json MyEvent> record, @Nullable Exception exception) {
        if (exception == null) {
            success(record.value());
        } else {
            fail(exception);
        }
    }
}
