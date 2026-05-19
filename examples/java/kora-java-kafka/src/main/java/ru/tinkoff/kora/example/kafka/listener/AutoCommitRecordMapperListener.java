package ru.tinkoff.kora.example.kafka.listener;

import java.io.IOException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.json.common.JsonReader;
import ru.tinkoff.kora.json.common.annotation.Json;
import ru.tinkoff.kora.kafka.common.annotation.KafkaListener;

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
            try {
                return reader.read(data);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @KafkaListener("kafka.consumer.my-listener")
    void process(ConsumerRecord<String, @Tag(MyEvent.class) MyEvent> record) {
        success(record.value());
    }
}
