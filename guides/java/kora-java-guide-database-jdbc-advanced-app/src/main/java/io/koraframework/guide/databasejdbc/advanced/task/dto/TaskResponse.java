package io.koraframework.guide.databasejdbc.advanced.task.dto;

import org.jspecify.annotations.Nullable;
import io.koraframework.guide.databasejdbc.advanced.dto.UserRequest;
import io.koraframework.guide.databasejdbc.advanced.repository.UserDAO;
import io.koraframework.json.common.annotation.Json;

import java.time.LocalDateTime;
import java.util.List;

@Json
public record TaskResponse(List<TaskCreated> tasks) {

    @Json
    public record TaskCreated(Long id,
                              String title,
                              @Nullable String description,
                              TaskStatus status,
                              @Nullable Long userAssigneeId,
                              LocalDateTime updatedAt) {
    }

    @Json
    public record TaskAssigned(Long id,
                               String title,
                               @Nullable String description,
                               TaskStatus status,
                               UserRequest assignee,
                               LocalDateTime updatedAt) {
    }
}
