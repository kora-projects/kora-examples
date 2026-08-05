package io.koraframework.guide.databasejdbc.advanced.task.dto;

import org.jspecify.annotations.Nullable;
import io.koraframework.json.common.annotation.Json;

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
