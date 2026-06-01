package ru.tinkoff.kora.example.graalvm.crud.r2dbc.repository.mapper;

import io.r2dbc.spi.Statement;
import jakarta.annotation.Nullable;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.database.r2dbc.mapper.parameter.R2dbcParameterColumnMapper;
import ru.tinkoff.kora.example.graalvm.crud.r2dbc.model.dao.Pet;

@Component
public final class PetStatusParameterMapper implements R2dbcParameterColumnMapper<Pet.Status> {

    @Override
    public void apply(Statement stmt, int index, @Nullable Pet.Status value) {
        if (value == null) {
            stmt.bindNull(index, Integer.class);
        } else {
            stmt.bind(index, value.code);
        }
    }
}
