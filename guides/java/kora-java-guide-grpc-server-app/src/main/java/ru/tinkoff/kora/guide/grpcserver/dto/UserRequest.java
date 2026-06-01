package ru.tinkoff.kora.guide.grpcserver.dto;

import ru.tinkoff.kora.json.common.annotation.Json;

@Json
public record UserRequest(String name, String email) {}
