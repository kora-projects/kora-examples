package io.koraframework.guide.databasejdbc.advanced.task.dto

import io.koraframework.json.common.annotation.Json

@Json
enum class TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE
}
