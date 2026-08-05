package io.koraframework.example.kafka.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import io.koraframework.common.annotation.Component;
import io.koraframework.kafka.common.annotation.KafkaListener;

@Component
public final class AutoCommitRecordListener extends AbstractListener<String> {

    @KafkaListener("kafka.consumer.my-listener")
    void process(ConsumerRecord<String, String> record) {
        success(record.value());
    }
}
