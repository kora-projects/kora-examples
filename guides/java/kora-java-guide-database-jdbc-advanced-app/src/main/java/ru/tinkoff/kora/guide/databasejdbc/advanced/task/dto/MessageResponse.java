package ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto;

import ru.tinkoff.kora.json.common.annotation.Json;

@Json
public record MessageResponse(String message) { }
