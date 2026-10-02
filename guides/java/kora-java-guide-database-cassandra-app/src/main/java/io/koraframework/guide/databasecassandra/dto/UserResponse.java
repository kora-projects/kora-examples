package io.koraframework.guide.databasecassandra.dto;

import java.time.Instant;
import io.koraframework.json.common.annotation.Json;

@Json
public record UserResponse(String id, String name, String email, Instant createdAt) {}
