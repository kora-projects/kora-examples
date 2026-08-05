package io.koraframework.guide.databasejdbc.advanced.task.dto

import io.koraframework.json.common.annotation.Json

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
