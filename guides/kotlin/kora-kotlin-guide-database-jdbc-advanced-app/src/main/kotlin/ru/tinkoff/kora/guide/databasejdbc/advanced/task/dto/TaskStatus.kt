package ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto

import ru.tinkoff.kora.json.common.annotation.Json

@Json
enum class TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE
}
