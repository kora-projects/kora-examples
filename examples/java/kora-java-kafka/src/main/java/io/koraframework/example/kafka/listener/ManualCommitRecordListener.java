package io.koraframework.example.kafka.listener;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import io.koraframework.common.annotation.Component;
import io.koraframework.kafka.common.annotation.KafkaListener;

@Component
public final class ManualCommitRecordListener extends AbstractListener<String> {

    @KafkaListener("kafka.consumer.my-listener")
    void process(ConsumerRecord<String, String> record, Consumer<String, String> consumer) {
        success(record.value());
        consumer.commitSync();
    }
}
