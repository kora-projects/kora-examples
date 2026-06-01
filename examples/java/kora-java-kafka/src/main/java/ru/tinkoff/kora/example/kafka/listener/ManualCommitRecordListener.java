package ru.tinkoff.kora.example.kafka.listener;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.kafka.common.annotation.KafkaListener;

@Component
public final class ManualCommitRecordListener extends AbstractListener<String> {

    @KafkaListener("kafka.consumer.my-listener")
    void process(ConsumerRecord<String, String> record, Consumer<String, String> consumer) {
        success(record.value());
        consumer.commitSync();
    }
}
