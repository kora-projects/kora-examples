package ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto;

import jakarta.annotation.Nullable;
import ru.tinkoff.kora.json.common.annotation.Json;

import java.util.List;

@Json
public record TaskRequest(List<TaskCreate> tasks) {

    @Json
    public record TaskCreate(String title,
                             @Nullable String description,
                             @Nullable Long userAssigneeId) { }
}
