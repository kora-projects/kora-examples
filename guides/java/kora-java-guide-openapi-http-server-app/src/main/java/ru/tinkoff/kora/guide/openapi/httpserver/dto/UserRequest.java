package ru.tinkoff.kora.guide.openapi.httpserver.dto;

import ru.tinkoff.kora.json.common.annotation.Json;

@Json
public record UserRequest(String name, String email) {}

