package io.koraframework.example.kafka.publisher;

import java.io.IOException;
import org.apache.kafka.common.serialization.Serializer;
import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Tag;
import io.koraframework.json.common.JsonWriter;
import io.koraframework.json.common.annotation.Json;
import io.koraframework.kafka.common.annotation.KafkaPublisher;
import io.koraframework.kafka.common.annotation.KafkaPublisher.Topic;

@KafkaPublisher("kafka.producer.my-publisher")
public interface TopicMapperPublisher {

    @Json
    record MyEvent(String username, int code) {}

    @Tag(MyEvent.class)
    @Component
    class MySerializer implements Serializer<MyEvent> {

        private final JsonWriter<MyEvent> writer;

        public MySerializer(JsonWriter<MyEvent> writer) {
            this.writer = writer;
        }

        @Override
        public byte[] serialize(String topic, MyEvent data) {
            try {
                return writer.toByteArray(data);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    @Topic("kafka.producer.my-topic")
    void send(@Tag(MyEvent.class) MyEvent value);
}
