package io.koraframework.guide.messaging.kafka.kafka;

import java.time.LocalDateTime;
import io.koraframework.json.common.annotation.Json;

@Json
public record UserCreatedEvent(String id, String name, String email, LocalDateTime createdAt) {}
