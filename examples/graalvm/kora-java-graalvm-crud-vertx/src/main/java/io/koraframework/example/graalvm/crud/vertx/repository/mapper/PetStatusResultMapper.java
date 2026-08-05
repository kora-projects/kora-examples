package io.koraframework.example.graalvm.crud.vertx.repository.mapper;

import io.koraframework.common.annotation.Component;
import io.koraframework.database.vertx.mapper.result.VertxResultColumnMapper;
import io.koraframework.example.graalvm.crud.vertx.model.dao.Pet;

@Component
public final class PetStatusResultMapper implements VertxResultColumnMapper<Pet.Status> {

    private final Pet.Status[] statuses = Pet.Status.values();

    @Override
    public Pet.Status apply(io.vertx.sqlclient.Row row, int index) {
        final int code = row.get(Integer.class, index);
        for (Pet.Status status : statuses) {
            if (code == status.code) {
                return status;
            }
        }

        throw new IllegalStateException("Unknown code: " + code);
    }
}
