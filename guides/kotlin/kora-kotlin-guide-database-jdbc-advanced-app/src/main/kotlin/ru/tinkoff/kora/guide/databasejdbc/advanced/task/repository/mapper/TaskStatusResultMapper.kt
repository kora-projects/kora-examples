package ru.tinkoff.kora.guide.databasejdbc.advanced.task.repository.mapper

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.database.jdbc.mapper.result.JdbcResultColumnMapper
import ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto.TaskStatus
import java.sql.ResultSet

@Component
class TaskStatusResultMapper : JdbcResultColumnMapper<TaskStatus> {

    override fun apply(row: ResultSet, index: Int): TaskStatus {
        val value = row.getString(index)
        return TaskStatus.valueOf(value)
    }
}
