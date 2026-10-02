package io.koraframework.example.kafka.listener;

import io.koraframework.common.annotation.Component;
import io.koraframework.json.common.annotation.Json;
import io.koraframework.kafka.common.annotation.KafkaListener;

import java.util.concurrent.CopyOnWriteArrayList;

@Component
public final class MalformedRecordListener {
    @Json public record Key(String id) {}
    @Json public record Value(String message) {}

    private final CopyOnWriteArrayList<Exception> failures = new CopyOnWriteArrayList<>();

    @KafkaListener("kafka.consumer.malformed-listener")
    public void process(@Json Key key, @Json Value value, Exception exception) {
        if (exception != null) {
            failures.add(exception);
        }
    }

    public CopyOnWriteArrayList<Exception> failures() { return failures; }
}
