package ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto

import ru.tinkoff.kora.json.common.annotation.Json

@Json
data class TaskRequest(
    val tasks: List<TaskCreate>
) {
    @Json
    data class TaskCreate(
        val title: String,
        val description: String?,
        val userAssigneeId: Long?
    )
}
