package io.koraframework.example.kafka.listener;

import io.koraframework.common.annotation.Component;
import io.koraframework.kafka.common.annotation.KafkaListener;
import io.koraframework.kafka.common.exceptions.KafkaSkipRecordException;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public final class SkipRecordListener {
    private final CopyOnWriteArrayList<String> processed = new CopyOnWriteArrayList<>();
    private final AtomicInteger skipped = new AtomicInteger();

    @KafkaListener("kafka.consumer.skip-listener")
    public void process(String value) {
        if (value.startsWith("skip:")) {
            skipped.incrementAndGet();
            throw new KafkaSkipRecordException(new IllegalArgumentException("Unsupported record: " + value));
        }
        processed.add(value);
    }

    public CopyOnWriteArrayList<String> processed() { return processed; }
    public int skipped() { return skipped.get(); }
}
