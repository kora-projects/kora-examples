package io.koraframework.example.submodule.pet.repository.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import io.koraframework.common.annotation.Component;
import io.koraframework.database.jdbc.mapper.result.JdbcResultColumnMapper;
import io.koraframework.example.submodule.pet.model.dao.Pet;

@Component
public final class PetStatusResultMapper implements JdbcResultColumnMapper<Pet.Status> {

    private final Pet.Status[] statuses = Pet.Status.values();

    @Override
    public Pet.Status apply(ResultSet row, int index) throws SQLException {
        final int code = row.getInt(index);
        for (Pet.Status status : statuses) {
            if (code == status.code) {
                return status;
            }
        }

        throw new IllegalStateException("Unknown code: " + code);
    }
}
