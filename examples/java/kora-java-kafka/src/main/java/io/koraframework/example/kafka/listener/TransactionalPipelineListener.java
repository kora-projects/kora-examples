package io.koraframework.example.kafka.listener;

import io.koraframework.common.annotation.Component;
import io.koraframework.example.kafka.publisher.MyTransactionalPublisher;
import io.koraframework.kafka.common.annotation.KafkaListener;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.Map;

@Component
public final class TransactionalPipelineListener {
    private final MyTransactionalPublisher publisher;

    public TransactionalPipelineListener(MyTransactionalPublisher publisher) {
        this.publisher = publisher;
    }

    @KafkaListener("kafka.consumer.transactional-pipeline")
    public void process(ConsumerRecord<String, String> record, Consumer<String, String> consumer) {
        publisher.withTx(transaction -> {
            transaction.publisher().send("processed:" + record.value());
            transaction.sendOffsetsToTransaction(
                Map.of(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset() + 1)),
                consumer.groupMetadata()
            );
        });
    }
}
