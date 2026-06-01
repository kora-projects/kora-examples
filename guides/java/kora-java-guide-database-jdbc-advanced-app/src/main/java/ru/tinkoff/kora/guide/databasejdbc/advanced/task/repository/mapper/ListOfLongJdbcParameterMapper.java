package ru.tinkoff.kora.guide.databasejdbc.advanced.task.repository.mapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.database.jdbc.mapper.parameter.JdbcParameterColumnMapper;

@Component
public final class ListOfLongJdbcParameterMapper implements JdbcParameterColumnMapper<List<Long>> {

    @Override
    public void set(PreparedStatement stmt, int index, List<Long> value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.ARRAY);
            return;
        }

        var sqlArray = stmt.getConnection().createArrayOf("BIGINT", value.toArray(Long[]::new));
        stmt.setArray(index, sqlArray);
    }
}
