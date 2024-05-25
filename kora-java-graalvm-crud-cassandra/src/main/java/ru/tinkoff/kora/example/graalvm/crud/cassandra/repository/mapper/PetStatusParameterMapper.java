package ru.tinkoff.kora.example.graalvm.crud.cassandra.repository.mapper;

import com.datastax.oss.driver.api.core.data.SettableByName;
import jakarta.annotation.Nullable;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.database.cassandra.mapper.parameter.CassandraParameterColumnMapper;
import ru.tinkoff.kora.example.graalvm.crud.cassandra.model.dao.Pet;

@Component
public final class PetStatusParameterMapper implements CassandraParameterColumnMapper<Pet.Status> {

    @Override
    public void apply(SettableByName<?> stmt, int index, @Nullable Pet.Status value) {
        if (value == null) {
            stmt.setToNull(index);
        } else {
            stmt.set(index, value.code, Integer.class);
        }
    }
}
