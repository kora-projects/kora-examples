package ru.tinkoff.kora.guide.messaging.kafka.kafka;

import java.time.LocalDateTime;
import ru.tinkoff.kora.json.common.annotation.Json;

@Json
public record UserCreatedEvent(String id, String name, String email, LocalDateTime createdAt) {}
