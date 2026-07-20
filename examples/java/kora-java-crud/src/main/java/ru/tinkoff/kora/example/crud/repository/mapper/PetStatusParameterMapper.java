package ru.tinkoff.kora.example.crud.repository.mapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import io.koraframework.common.annotation.Component;
import io.koraframework.database.jdbc.mapper.parameter.JdbcParameterColumnMapper;
import ru.tinkoff.kora.example.crud.model.dao.Pet;

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
