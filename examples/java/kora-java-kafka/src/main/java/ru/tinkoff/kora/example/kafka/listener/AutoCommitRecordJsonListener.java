package ru.tinkoff.kora.example.kafka.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.json.common.annotation.Json;
import ru.tinkoff.kora.kafka.common.annotation.KafkaListener;

@Component
public final class AutoCommitRecordJsonListener extends AbstractListener<AutoCommitRecordJsonListener.MyEvent> {

    @Json
    public record MyEvent(String username, int code) {}

    @KafkaListener("kafka.consumer.my-listener")
    void process(ConsumerRecord<String, @Json MyEvent> record) {
        success(record.value());
    }
}
