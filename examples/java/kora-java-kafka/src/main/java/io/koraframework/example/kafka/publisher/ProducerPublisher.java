package io.koraframework.example.kafka.publisher;

import org.apache.kafka.clients.producer.ProducerRecord;
import io.koraframework.kafka.common.annotation.KafkaPublisher;

@KafkaPublisher("kafka.producer.my-publisher")
public interface ProducerPublisher {

    void send(ProducerRecord<String, String> record);
}
