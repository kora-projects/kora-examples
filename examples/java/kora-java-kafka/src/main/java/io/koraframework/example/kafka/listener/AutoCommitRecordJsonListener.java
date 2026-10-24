package io.koraframework.example.kafka.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import io.koraframework.common.annotation.Component;
import io.koraframework.json.common.annotation.Json;
import io.koraframework.kafka.common.annotation.KafkaListener;

@Component
public final class AutoCommitRecordJsonListener extends AbstractListener<AutoCommitRecordJsonListener.MyEvent> {

    @Json
    public record MyEvent(String username, int code) {}

    @KafkaListener("kafka.consumer.my-listener")
    void process(ConsumerRecord<String, @Json MyEvent> record) {
        success(record.value());
    }
}
