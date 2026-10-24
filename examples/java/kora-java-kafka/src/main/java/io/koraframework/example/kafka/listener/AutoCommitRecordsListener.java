package io.koraframework.example.kafka.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import io.koraframework.common.annotation.Component;
import io.koraframework.kafka.common.annotation.KafkaListener;

@Component
public final class AutoCommitRecordsListener extends AbstractListener<String> {

    @KafkaListener("kafka.consumer.my-listener")
    void process(ConsumerRecords<String, String> records) {
        for (ConsumerRecord<String, String> record : records) {
            success(record.value());
        }
    }
}
