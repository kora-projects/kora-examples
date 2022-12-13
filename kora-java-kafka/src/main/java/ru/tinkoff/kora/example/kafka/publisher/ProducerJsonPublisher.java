package ru.tinkoff.kora.example.kafka.publisher;

import static ru.tinkoff.kora.example.kafka.publisher.ProducerJsonPublisher.MyEvent;

import org.apache.kafka.clients.producer.ProducerRecord;
import ru.tinkoff.kora.json.common.annotation.Json;
import ru.tinkoff.kora.kafka.common.annotation.KafkaPublisher;

@KafkaPublisher("kafka.producer.my-publisher")
public interface ProducerJsonPublisher {

    @Json
    record MyEvent(String username, int code) {}

    void send(ProducerRecord<String, @Json MyEvent> record);
}
