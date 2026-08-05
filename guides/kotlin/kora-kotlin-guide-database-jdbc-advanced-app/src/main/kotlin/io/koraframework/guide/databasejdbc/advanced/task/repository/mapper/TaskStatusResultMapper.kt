package io.koraframework.guide.databasejdbc.advanced.task.repository.mapper

import io.koraframework.common.annotation.Component
import io.koraframework.database.jdbc.mapper.result.JdbcResultColumnMapper
import io.koraframework.guide.databasejdbc.advanced.task.dto.TaskStatus
import java.sql.ResultSet

@Component
class TaskStatusResultMapper : JdbcResultColumnMapper<TaskStatus> {

    override fun apply(row: ResultSet, index: Int): TaskStatus {
        val value = row.getString(index)
        return TaskStatus.valueOf(value)
    }
}
