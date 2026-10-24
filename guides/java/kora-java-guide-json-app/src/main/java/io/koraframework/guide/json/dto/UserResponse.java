package io.koraframework.guide.json.dto;

import java.time.LocalDateTime;
import io.koraframework.json.common.annotation.Json;

@Json
public record UserResponse(String id, String name, String email, LocalDateTime createdAt) {}
