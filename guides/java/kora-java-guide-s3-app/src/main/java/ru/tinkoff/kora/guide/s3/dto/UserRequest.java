package ru.tinkoff.kora.guide.s3.dto;

import ru.tinkoff.kora.json.common.annotation.Json;

@Json
public record UserRequest(String name, String email) {}

