package ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto;

import jakarta.annotation.Nullable;
import ru.tinkoff.kora.guide.databasejdbc.advanced.dto.UserRequest;
import ru.tinkoff.kora.guide.databasejdbc.advanced.repository.UserDAO;
import ru.tinkoff.kora.json.common.annotation.Json;

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
