package io.koraframework.example.graalvm.crud.cassandra.repository.mapper;

import com.datastax.oss.driver.api.core.data.SettableByName;
import org.jspecify.annotations.Nullable;
import io.koraframework.common.annotation.Component;
import io.koraframework.database.cassandra.mapper.parameter.CassandraParameterColumnMapper;
import io.koraframework.example.graalvm.crud.cassandra.model.dao.Pet;

@Component
public final class PetStatusParameterMapper implements CassandraParameterColumnMapper<Pet.Status> {

    @Override
    public void apply(SettableByName<?> stmt, int index, Pet.@Nullable Status value) {
        if (value == null) {
            stmt.setToNull(index);
        } else {
            stmt.set(index, value.code, Integer.class);
        }
    }
}
