package ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto

import ru.tinkoff.kora.guide.databasejdbc.advanced.dto.UserRequest
import ru.tinkoff.kora.json.common.annotation.Json
import java.time.LocalDateTime

@Json
data class TaskResponse(
    val tasks: List<TaskCreated>
) {
    @Json
    data class TaskCreated(
        val id: Long,
        val title: String,
        val description: String?,
        val status: TaskStatus,
        val userAssigneeId: Long?,
        val updatedAt: LocalDateTime
    )

    @Json
    data class TaskAssigned(
        val id: Long,
        val title: String,
        val description: String?,
        val status: TaskStatus,
        val assignee: UserRequest,
        val updatedAt: LocalDateTime
    )
}
