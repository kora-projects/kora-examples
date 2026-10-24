package io.koraframework.guide.databasejdbc.advanced.task.repository.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import io.koraframework.common.annotation.Component;
import io.koraframework.database.jdbc.mapper.result.JdbcResultColumnMapper;
import io.koraframework.guide.databasejdbc.advanced.task.dto.TaskStatus;

@Component
public final class TaskStatusResultMapper implements JdbcResultColumnMapper<TaskStatus> {

    @Override
    public TaskStatus apply(ResultSet row, int index) throws SQLException {
        var value = row.getString(index);
        return value == null ? null : TaskStatus.valueOf(value);
    }
}
