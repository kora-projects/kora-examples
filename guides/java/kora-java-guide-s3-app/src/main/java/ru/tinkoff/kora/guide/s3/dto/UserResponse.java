package ru.tinkoff.kora.guide.s3.dto;

import java.time.LocalDateTime;
import ru.tinkoff.kora.json.common.annotation.Json;

@Json
public record UserResponse(String id, String name, String email, LocalDateTime createdAt) {}

