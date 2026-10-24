package io.koraframework.guide.databasejdbc.advanced.task.repository.mapper

import io.koraframework.common.annotation.Component
import io.koraframework.database.jdbc.mapper.parameter.JdbcParameterColumnMapper
import io.koraframework.guide.databasejdbc.advanced.task.dto.TaskStatus
import java.sql.PreparedStatement
import java.sql.Types

@Component
class TaskStatusParameterMapper : JdbcParameterColumnMapper<TaskStatus> {

    override fun set(stmt: PreparedStatement, index: Int, value: TaskStatus?) {
        if (value == null) {
            stmt.setNull(index, Types.VARCHAR)
        } else {
            stmt.setString(index, value.name)
        }
    }
}
