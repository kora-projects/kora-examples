package io.koraframework.guide.databasejdbc.advanced.task.repository

import io.koraframework.database.common.annotation.Column
import io.koraframework.database.common.annotation.Embedded
import io.koraframework.database.common.annotation.Id
import io.koraframework.database.common.annotation.Table
import io.koraframework.database.jdbc.annotation.EntityJdbc
import io.koraframework.guide.databasejdbc.advanced.repository.UserDAO
import io.koraframework.guide.databasejdbc.advanced.task.dto.TaskStatus
import java.time.LocalDateTime

@EntityJdbc
@Table("tasks")
data class TaskDAO(
    @field:Column("title") val title: String,
    @field:Column("status") val status: TaskStatus,
    @field:Column("description") val description: String?,
    @field:Column("user_assignee_id") val userAssigneeId: Long?
) {
    @EntityJdbc
    data class SelectAssigned(
        @field:Column("task_id") @field:Id val id: Long,
        @field:Column("created_at") val createdAt: LocalDateTime,
        @field:Column("updated_at") val updatedAt: LocalDateTime,
        @field:Embedded("assignee_") val assigned: UserDAO,
        @field:Embedded val base: TaskDAO
    )
}
