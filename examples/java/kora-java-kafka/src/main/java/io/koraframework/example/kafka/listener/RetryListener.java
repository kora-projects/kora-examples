package io.koraframework.example.kafka.listener;

import io.koraframework.common.annotation.Component;
import io.koraframework.kafka.common.annotation.KafkaListener;
import io.koraframework.resilient.retry.annotation.Retryable;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RetryListener {
    private final AtomicInteger attempts = new AtomicInteger();
    private final CopyOnWriteArrayList<String> processed = new CopyOnWriteArrayList<>();

    @Retryable(KafkaListenerRetry.class)
    @KafkaListener("kafka.consumer.retry-listener")
    public void process(String value) {
        if (attempts.incrementAndGet() < 3) {
            throw new IllegalStateException("Transient processing failure");
        }
        processed.add(value);
    }

    public int attempts() { return attempts.get(); }
    public CopyOnWriteArrayList<String> processed() { return processed; }
}
