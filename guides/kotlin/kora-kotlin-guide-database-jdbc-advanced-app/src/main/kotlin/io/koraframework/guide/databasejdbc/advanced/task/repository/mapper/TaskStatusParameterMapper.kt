package io.koraframework.guide.databasejdbc.advanced.task.repository.mapper

import io.koraframework.common.annotation.Component
import io.koraframework.database.jdbc.mapper.parameter.JdbcParameterColumnMapper
import io.koraframework.guide.databasejdbc.advanced.task.dto.TaskStatus
import java.sql.PreparedStatement

@Component
class TaskStatusParameterMapper : JdbcParameterColumnMapper<TaskStatus> {

    override fun set(stmt: PreparedStatement, index: Int, value: TaskStatus) {
        stmt.setString(index, value.name)
    }
}
