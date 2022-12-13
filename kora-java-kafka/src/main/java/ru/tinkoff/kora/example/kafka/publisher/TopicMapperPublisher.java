package ru.tinkoff.kora.example.kafka.publisher;

import java.io.IOException;
import org.apache.kafka.common.serialization.Serializer;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.json.common.JsonWriter;
import ru.tinkoff.kora.json.common.annotation.Json;
import ru.tinkoff.kora.kafka.common.annotation.KafkaPublisher;

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

    @KafkaPublisher.Topic("kafka.producer.my-topic")
    void send(@Tag(MyEvent.class) MyEvent value);
}
