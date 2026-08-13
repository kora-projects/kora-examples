package io.koraframework.example.kafka.publisher;

import static io.koraframework.example.kafka.publisher.ProducerMapperPublisher.MyEvent;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serializer;
import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Tag;
import io.koraframework.json.common.JsonWriter;
import io.koraframework.json.common.annotation.Json;
import io.koraframework.kafka.common.annotation.KafkaPublisher;

@KafkaPublisher("kafka.producer.my-publisher")
public interface ProducerMapperPublisher {

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
            return writer.toByteArray(data);
        }
    }

    void send(ProducerRecord<String, @Tag(MyEvent.class) MyEvent> record);
}
