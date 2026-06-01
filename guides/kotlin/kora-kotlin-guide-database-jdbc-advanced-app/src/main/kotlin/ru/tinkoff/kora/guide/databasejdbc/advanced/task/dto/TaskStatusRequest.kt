package ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto

import ru.tinkoff.kora.json.common.annotation.Json

@Json
data class TaskStatusRequest(
    val status: TaskStatus
)
