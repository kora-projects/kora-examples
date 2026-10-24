package io.koraframework.guide.databasejdbc.advanced.task.dto;

import org.jspecify.annotations.Nullable;
import io.koraframework.json.common.annotation.Json;

import java.util.List;

@Json
public record TaskRequest(List<TaskCreate> tasks) {

    @Json
    public record TaskCreate(String title,
                             @Nullable String description,
                             @Nullable Long userAssigneeId) { }
}
