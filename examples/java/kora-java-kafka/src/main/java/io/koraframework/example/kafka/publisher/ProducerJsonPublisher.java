package io.koraframework.example.kafka.publisher;

import static io.koraframework.example.kafka.publisher.ProducerJsonPublisher.MyEvent;

import org.apache.kafka.clients.producer.ProducerRecord;
import io.koraframework.json.common.annotation.Json;
import io.koraframework.kafka.common.annotation.KafkaPublisher;

@KafkaPublisher("kafka.producer.my-publisher")
public interface ProducerJsonPublisher {

    @Json
    record MyEvent(String username, int code) {}

    void send(ProducerRecord<String, @Json MyEvent> record);
}
