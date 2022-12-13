package ru.tinkoff.kora.example.kafka.listener;

import static ru.tinkoff.kora.example.kafka.listener.AutoCommitRecordExceptionListener.MyEvent;

import jakarta.annotation.Nullable;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.json.common.annotation.Json;
import ru.tinkoff.kora.kafka.common.annotation.KafkaListener;

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
