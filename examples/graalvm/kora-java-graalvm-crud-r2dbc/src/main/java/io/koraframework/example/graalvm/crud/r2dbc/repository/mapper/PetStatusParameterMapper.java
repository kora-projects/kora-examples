package io.koraframework.example.graalvm.crud.r2dbc.repository.mapper;

import io.r2dbc.spi.Statement;
import org.jspecify.annotations.Nullable;
import io.koraframework.common.annotation.Component;
import io.koraframework.database.r2dbc.mapper.parameter.R2dbcParameterColumnMapper;
import io.koraframework.example.graalvm.crud.r2dbc.model.dao.Pet;

@Component
public final class PetStatusParameterMapper implements R2dbcParameterColumnMapper<Pet.Status> {

    @Override
    public void apply(Statement stmt, int index, Pet.@Nullable Status value) {
        if (value == null) {
            stmt.bindNull(index, Integer.class);
        } else {
            stmt.bind(index, value.code);
        }
    }
}
