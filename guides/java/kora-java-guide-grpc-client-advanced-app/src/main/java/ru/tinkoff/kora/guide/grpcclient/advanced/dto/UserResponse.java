package ru.tinkoff.kora.guide.grpcclient.advanced.dto;

import java.time.LocalDateTime;
import ru.tinkoff.kora.json.common.annotation.Json;

@Json
public record UserResponse(String id, String name, String email, LocalDateTime createdAt) {}
