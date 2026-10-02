package io.koraframework.guide.messaging.kafka.kafka;

import io.koraframework.json.common.annotation.Json;
import io.koraframework.kafka.common.annotation.KafkaPublisher;
import io.koraframework.kafka.common.annotation.KafkaPublisher.Topic;

@KafkaPublisher("kafka.producer.user-created")
public interface UserCreatedPublisher {

    @Topic("kafka.producer.user-created-topic")
    void send(@Json UserCreatedEvent event);
}
