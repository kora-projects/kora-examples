package io.koraframework.guide.s3.dto;

import java.time.LocalDateTime;
import io.koraframework.json.common.annotation.Json;

@Json
public record UserResponse(String id, String name, String email, LocalDateTime createdAt) {}

