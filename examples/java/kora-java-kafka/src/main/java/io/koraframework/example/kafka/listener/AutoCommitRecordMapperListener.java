package io.koraframework.example.kafka.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Tag;
import io.koraframework.json.common.JsonReader;
import io.koraframework.json.common.annotation.Json;
import io.koraframework.kafka.common.annotation.KafkaListener;

@Component
public final class AutoCommitRecordMapperListener extends AbstractListener<AutoCommitRecordMapperListener.MyEvent> {

    @Json
    public record MyEvent(String username, int code) {}

    @Tag(MyEvent.class)
    @Component
    public static class MyDeserializer implements Deserializer<MyEvent> {

        private final JsonReader<MyEvent> reader;

        public MyDeserializer(JsonReader<MyEvent> reader) {
            this.reader = reader;
        }

        @Override
        public MyEvent deserialize(String topic, byte[] data) {
            return reader.read(data);
        }
    }

    @KafkaListener("kafka.consumer.my-listener")
    void process(ConsumerRecord<String, @Tag(MyEvent.class) MyEvent> record) {
        success(record.value());
    }
}
