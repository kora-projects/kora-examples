package ru.tinkoff.kora.example.graalvm.crud.r2dbc.repository.mapper;

import io.r2dbc.spi.Row;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.database.r2dbc.mapper.result.R2dbcResultColumnMapper;
import ru.tinkoff.kora.example.graalvm.crud.r2dbc.model.dao.Pet;

@Component
public final class PetStatusResultMapper implements R2dbcResultColumnMapper<Pet.Status> {

    private final Pet.Status[] statuses = Pet.Status.values();

    @Override
    public Pet.Status apply(Row row, String label) {
        final int code = row.get(label, Integer.class);
        for (Pet.Status status : statuses) {
            if (code == status.code) {
                return status;
            }
        }

        throw new IllegalStateException("Unknown code: " + code);
    }
}
