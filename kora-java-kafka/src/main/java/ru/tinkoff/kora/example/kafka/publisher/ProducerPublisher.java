package ru.tinkoff.kora.example.kafka.publisher;

import org.apache.kafka.clients.producer.ProducerRecord;
import ru.tinkoff.kora.kafka.common.annotation.KafkaPublisher;

@KafkaPublisher("kafka.producer.my-publisher")
public interface ProducerPublisher {

    void send(ProducerRecord<String, String> record);
}
