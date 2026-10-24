package io.koraframework.guide.databasejdbc.advanced.task.repository.mapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import io.koraframework.common.annotation.Component;
import io.koraframework.database.jdbc.mapper.parameter.JdbcParameterColumnMapper;

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
