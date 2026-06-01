package ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto;

import jakarta.annotation.Nullable;
import ru.tinkoff.kora.json.common.annotation.Json;

@Json
public record TaskWithAssigneeResponse(
        String id,
        String title,
        @Nullable String description,
        TaskStatus status,
        @Nullable Assignee assignee) {

    @Json
    public record Assignee(String id, String name) {}
}
