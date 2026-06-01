package ru.tinkoff.kora.guide.databasejdbc.advanced.task.repository.mapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.database.jdbc.mapper.parameter.JdbcParameterColumnMapper;
import ru.tinkoff.kora.guide.databasejdbc.advanced.task.dto.TaskStatus;

@Component
public final class TaskStatusParameterMapper implements JdbcParameterColumnMapper<TaskStatus> {

    @Override
    public void set(PreparedStatement stmt, int index, TaskStatus value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.VARCHAR);
        } else {
            stmt.setString(index, value.name());
        }
    }
}
