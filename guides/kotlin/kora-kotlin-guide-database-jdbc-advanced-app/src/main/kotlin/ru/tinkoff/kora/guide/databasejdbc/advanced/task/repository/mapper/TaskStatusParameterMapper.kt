package ru.tinkoff.kora.guide.databasejdbc.advanced.task.repository.mapper

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.database.jdbc.mapper.parameter.JdbcParameterColumnMapper
import ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto.TaskStatus
import java.sql.PreparedStatement

@Component
class TaskStatusParameterMapper : JdbcParameterColumnMapper<TaskStatus> {

    override fun set(stmt: PreparedStatement, index: Int, value: TaskStatus) {
        stmt.setString(index, value.name)
    }
}
