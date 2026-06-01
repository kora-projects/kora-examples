package ru.tinkoff.kora.guide.observability.dto;

import ru.tinkoff.kora.json.common.annotation.Json;

@Json
public record UserRequest(String name, String email) {}
