package ru.tinkoff.kora.guide.databasejdbc.advanced.task.repository

import ru.tinkoff.kora.database.common.annotation.Column
import ru.tinkoff.kora.database.common.annotation.Embedded
import ru.tinkoff.kora.database.common.annotation.Id
import ru.tinkoff.kora.database.common.annotation.Table
import ru.tinkoff.kora.database.jdbc.EntityJdbc
import ru.tinkoff.kora.guide.databasejdbc.advanced.repository.UserDAO
import ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto.TaskStatus
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
