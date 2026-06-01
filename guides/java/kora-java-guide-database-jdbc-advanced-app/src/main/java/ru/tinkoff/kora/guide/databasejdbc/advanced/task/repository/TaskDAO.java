package ru.tinkoff.kora.guide.databasejdbc.advanced.task.repository;

import jakarta.annotation.Nullable;
import ru.tinkoff.kora.database.common.annotation.Column;
import ru.tinkoff.kora.database.common.annotation.Embedded;
import ru.tinkoff.kora.database.common.annotation.Id;
import ru.tinkoff.kora.database.common.annotation.Table;
import ru.tinkoff.kora.database.jdbc.EntityJdbc;
import ru.tinkoff.kora.guide.databasejdbc.advanced.repository.UserDAO;
import ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto.TaskStatus;

import java.time.LocalDateTime;

@EntityJdbc
@Table("tasks")
public record TaskDAO(
        @Column("title") String title,
        @Column("status") TaskStatus status,
        @Column("description") @Nullable String description,
        @Column("user_assignee_id") @Nullable Long userAssigneeId) {

    @EntityJdbc
    public record SelectAssigned(
            @Column("task_id") @Id Long id,
            @Column("created_at") LocalDateTime createdAt,
            @Column("updated_at") LocalDateTime updatedAt,
            @Embedded("assignee_") UserDAO assigned,
            @Embedded TaskDAO base) {
    }
}
