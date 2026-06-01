package ru.tinkoff.kora.guide.validation.dto;

import ru.tinkoff.kora.json.common.annotation.Json;

@Json
public record ValidationErrorDetails(String field, String message) {}
