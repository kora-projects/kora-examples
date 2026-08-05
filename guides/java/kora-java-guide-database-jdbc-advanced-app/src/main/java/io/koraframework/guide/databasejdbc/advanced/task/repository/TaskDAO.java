package io.koraframework.guide.databasejdbc.advanced.task.repository;

import org.jspecify.annotations.Nullable;
import io.koraframework.database.common.annotation.Column;
import io.koraframework.database.common.annotation.Embedded;
import io.koraframework.database.common.annotation.Id;
import io.koraframework.database.common.annotation.Table;
import io.koraframework.database.jdbc.annotation.EntityJdbc;
import io.koraframework.guide.databasejdbc.advanced.repository.UserDAO;
import io.koraframework.guide.databasejdbc.advanced.task.dto.TaskStatus;

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
