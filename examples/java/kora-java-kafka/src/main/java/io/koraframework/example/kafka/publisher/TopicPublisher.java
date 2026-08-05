package io.koraframework.example.kafka.publisher;

import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.RecordMetadata;
import io.koraframework.kafka.common.annotation.KafkaPublisher;
import io.koraframework.kafka.common.annotation.KafkaPublisher.Topic;

@KafkaPublisher("kafka.producer.my-publisher")
public interface TopicPublisher {

    @Topic("kafka.producer.my-topic")
    void send(String value);

    @Topic("kafka.producer.my-topic")
    RecordMetadata sendMeta(String value);

    @Topic("kafka.producer.my-topic")
    Future<RecordMetadata> sendMetaAsync(String value);
}
