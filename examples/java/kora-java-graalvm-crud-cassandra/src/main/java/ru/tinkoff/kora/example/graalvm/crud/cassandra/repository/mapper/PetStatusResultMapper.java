package ru.tinkoff.kora.example.graalvm.crud.cassandra.repository.mapper;

import com.datastax.oss.driver.api.core.data.GettableByName;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.database.cassandra.mapper.result.CassandraRowColumnMapper;
import ru.tinkoff.kora.example.graalvm.crud.cassandra.model.dao.Pet;

@Component
public final class PetStatusResultMapper implements CassandraRowColumnMapper<Pet.Status> {

    private final Pet.Status[] statuses = Pet.Status.values();

    @Override
    public Pet.Status apply(GettableByName row, int index) {
        final int code = row.get(index, Integer.class);
        for (Pet.Status status : statuses) {
            if (code == status.code) {
                return status;
            }
        }

        throw new IllegalStateException("Unknown code: " + code);
    }
}
