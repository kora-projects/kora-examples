package ru.tinkoff.kora.example.graalvm.crud.jdbc.repository.mapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.database.jdbc.mapper.parameter.JdbcParameterColumnMapper;
import ru.tinkoff.kora.example.graalvm.crud.jdbc.model.dao.Pet;

@Component
public final class PetStatusParameterMapper implements JdbcParameterColumnMapper<Pet.Status> {

    @Override
    public void set(PreparedStatement stmt, int index, Pet.Status value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.INTEGER);
        } else {
            stmt.setInt(index, value.code);
        }
    }
}
