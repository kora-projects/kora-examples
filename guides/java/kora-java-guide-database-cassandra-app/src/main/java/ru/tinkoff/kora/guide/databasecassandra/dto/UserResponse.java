package ru.tinkoff.kora.guide.databasecassandra.dto;

import java.time.Instant;
import ru.tinkoff.kora.json.common.annotation.Json;

@Json
public record UserResponse(String id, String name, String email, Instant createdAt) {}
