package io.koraframework.example.kafka.listener;

import io.koraframework.resilient.retry.Retry;
import io.koraframework.resilient.retry.annotation.RetrySpec;

@RetrySpec("resilient.retry.kafka-listener")
public interface KafkaListenerRetry extends Retry {}
