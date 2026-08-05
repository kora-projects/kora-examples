package io.koraframework.guide.databasejdbc.advanced.task.dto

import io.koraframework.json.common.annotation.Json

@Json
data class TaskStatusRequest(
    val status: TaskStatus
)
