package ru.tinkoff.kora.guide.databasejdbc.advanced.task.repository.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.database.jdbc.mapper.result.JdbcResultColumnMapper;
import ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto.TaskStatus;

@Component
public final class TaskStatusResultMapper implements JdbcResultColumnMapper<TaskStatus> {

    @Override
    public TaskStatus apply(ResultSet row, int index) throws SQLException {
        var value = row.getString(index);
        return value == null ? null : TaskStatus.valueOf(value);
    }
}
