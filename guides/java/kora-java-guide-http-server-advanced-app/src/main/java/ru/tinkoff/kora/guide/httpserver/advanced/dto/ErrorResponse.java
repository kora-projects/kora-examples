package ru.tinkoff.kora.guide.httpserver.advanced.dto;

import ru.tinkoff.kora.json.common.annotation.Json;

@Json
public record ErrorResponse(String message) {}
